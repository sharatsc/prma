package com.prma.workers;
import com.amazonaws.services.sqs.model.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.prma.Solvers.SGDConfig;
import com.prma.Structures.GeneralizedLinearModel;
import com.prma.Structures.Instance;
import com.prma.Structures.ModelConfig;
import com.prma.Structures.SerializedModel;
import com.prma.Structures.Weight;
import com.prma.aws.*;
import com.prma.backend.KeyValueStore;
import com.prma.parsers.*;
import com.prma.solvers.*;
import com.prma.API.*;

import java.io.File;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// TODO(sharat): Make this strategy more intelligent to choose parameters based on input data.
// TODO(sharat): Meta-classifier patentable?
class ModelStrategy {
	public OnlineSolver getSolver(TrainingRequest request){
		// Pick the only solver we have.
		OnlineSolver solver = new SGDSolver();
		ModelConfig.Builder modelConfig = ModelConfig.newBuilder();
		switch(request.getModelType()) {
			case CLASSIFICATION:
				modelConfig.setRegressionType(ModelConfig.RegressionType.LOGISTIC_REGRESSION);
				break;
			case REGRESSION:
				modelConfig.setRegressionType(ModelConfig.RegressionType.LINEAR_REGRESSION);
				break;
			case MULTI_CLASSIFICATION:
				modelConfig.setRegressionType(ModelConfig.RegressionType.LOGISTIC_REGRESSION);
				break;
		}
		SGDConfig.Builder solverConfig = SGDConfig.newBuilder();
		solverConfig.setLearningRate((float) 0.2);
		solverConfig.setL2Regularization((float) 0.05);
		modelConfig.setExtension(
				SGDConfig.sgdConfig,
				solverConfig.build());
		solver.Initialize(modelConfig.build());
		return solver;
	}
	
	public OnlineMetrics getOnlineMetrics(TrainingRequest request) {
		switch(request.getModelType()) {
			case CLASSIFICATION:
				return new AccuracyMetrics();
			case MULTI_CLASSIFICATION:
				return new AccuracyMetrics();
			case REGRESSION:
				return new MSEMetrics();
		}
		return null;
	}
	
	public InputStream getInputStream(String path) throws URISyntaxException, IOException {
		AWSStorage s3 = new AWSStorage();
		if (path.startsWith("s3:")) {
			return s3.getObject(path);
		} else if (path.startsWith("http:")) {
			URL url = new URL(path);
			return url.openStream();
		} else {
			return new FileInputStream(path);
		}
	}
	public String getCacheFile(TrainingRequest request) {
		return request.getModelId() + ".cache";
	}
	public InstanceIterator getIterator(TrainingRequest request) throws URISyntaxException, IOException {
		ChainedInstanceIterator chainedIterator = new ChainedInstanceIterator();
		int numFiles = request.getInputFileCount();
		for (int i = 0; i < numFiles; ++i) {
			System.out.println(request.getInputFile(i));
			InstanceIterator iterator = null;
			InputStream inputStream = getInputStream(request.getInputFile(i));
			if (inputStream == null) {
				continue;
			}
			switch(request.getInputType()) {
			case CSV:
				iterator = new CSVInstanceIterator();
				break;
			case LIBSVM:
				iterator = new LibSVMInstanceIterator();
				break;
			case TEXT:
				iterator = new StringInstanceIterator();
				break;
			default:
				System.out.println("We should not be here");
				iterator = new LibSVMInstanceIterator();
			}
			iterator.Initialize(inputStream);
			chainedIterator.addIterator(iterator);
		}
		return chainedIterator;
	}

}

public class TrainingWorker {

	public static double getSign(double x) {
		if (x < 0) return -1;
		return 1;
	}
	
	/**
	 * @param args
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws InvalidInputDataException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
		AWSQueue queue = new AWSQueue();
		AWSDB db = new AWSDB();
		AWSStorage s3 = new AWSStorage();
		ModelStrategy modelStrategy = new ModelStrategy();
		
		while(true) {
			Message message = queue.getNextMessage(AWSQueue.TrainingQueue , 30);
			if(message == null) {
				System.out.println("No pending tasks. sleeping..");
				Thread.sleep(10000);
				continue;
			}
			TrainingRequest.Builder request = TrainingRequest.newBuilder();
			JsonFormat.merge(message.getBody(), request);
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			db.setAttribute(request.getModelId(), "Status", "TRAINING");
			db.setAttribute(request.getModelId(), "Training started", dateFormat.format(new Date()));
			// Train model
			TrainingRequest trainingRequest = request.build();
			OnlineSolver solver = modelStrategy.getSolver(trainingRequest);
			OnlineMetrics metrics = modelStrategy.getOnlineMetrics(trainingRequest);
			OutputStream cacheOutput = new FileOutputStream(modelStrategy.getCacheFile(trainingRequest));
			InstanceIterator iterator = null;
			for (int iteration = 0; iteration < request.getMaxIterations(); ++iteration) {
				metrics.resetMetrics();
				System.out.println("Iteration:" + iteration);
				if (iteration == 0) {
					queue.deleteMessage(message);
					iterator = modelStrategy.getIterator(trainingRequest);
					while(iterator.hasNextInstance()) {
						Instance instance = iterator.nextInstance();
						solver.TrainOnInstance(instance, 0);
						metrics.accumulateMetrics(instance, solver.PredictOnInstance(instance));
						instance.writeDelimitedTo(cacheOutput);
					}
				} else {
					System.out.println("Reading from cache.");
					InputStream cacheInput = new FileInputStream(
							modelStrategy.getCacheFile(trainingRequest));
					Instance instance = null;
					while((instance = Instance.parseDelimitedFrom(cacheInput)) != null) {
						solver.TrainOnInstance(instance, iteration);
						metrics.accumulateMetrics(instance, solver.PredictOnInstance(instance));
					}
				}
				// Save model
				db.setAttribute(request.getModelId(), "Training ended", dateFormat.format(new Date()));
				db.setAttribute(request.getModelId(), "Accuracy", "" + metrics.getFinalMetrics());
				SerializedModel model = solver.getModel();
				System.out.println(model.toString());
				{
					File tmpFile = File.createTempFile(request.getModelId(), ".model");
					model.writeTo(new FileOutputStream(tmpFile));
					s3.putObject(new FileInputStream(tmpFile.getAbsolutePath()), "sharat", "models/" +
					request.getModelId());
				}
				// Push model to key-val
				if (model.hasLinearModel()) {
					GeneralizedLinearModel linearModel = model.getLinearModel();
					Map<String, String> keyValues = new HashMap<String, String>();
					keyValues.put(request.getModelId() + ":" + "bias",
							JsonFormat.printToString(linearModel.getBias()));
					for (Weight weight: linearModel.getWeightsList()) {
						String key = request.getModelId() + ":" + weight.getIndex();
						String value = JsonFormat.printToString(weight);
						System.out.println(key + ":" + value);
						keyValues.put(key, value);
					}
					System.out.println(keyValues.toString());
					KeyValueStore keyValueStore = new AWSDynamo();
					keyValueStore.batchSetKeyValues(keyValues);
				}
				db.setAttribute(request.getModelId(), "Status", "READY");
			}
		}
	}
}
