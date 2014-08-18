package com.prma.solvers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.prma.Solvers.SGDConfig;
import com.prma.Solvers.MultiSGDConfig;
import com.prma.Structures.Instance;
import com.prma.Structures.ModelConfig;
import com.prma.Structures.SerializedModel;
import com.prma.Structures.MulticlassModel;

public class MulticlassSGDSolver implements OnlineSolver {
	int _num_classes = 0;
	List<SGDSolver> _solvers = new ArrayList<SGDSolver>();
	MulticlassModel.Builder _model = MulticlassModel.newBuilder();
	float _learningRate = (float) 0.1;
	float _l2Regularization = (float) 0.1;
	float _l1Regularization = (float) 0.0;
	boolean _modelUpdated = true;

	@Override
	public boolean Initialize(ModelConfig readOnlyConfig) {
		ModelConfig.Builder config = ModelConfig.newBuilder(readOnlyConfig);
		if (config.hasExtension(
				MultiSGDConfig.multiSgdConfig)) {
			MultiSGDConfig solverConfig =
					config.getExtension(
							MultiSGDConfig.multiSgdConfig);
			_num_classes = solverConfig.getNumClasses();
			_learningRate = solverConfig.getLearningRate();
			_l2Regularization = solverConfig.getL2Regularization();
			_l1Regularization = solverConfig.getL1Regularization();
			// Copy to individual solvers
			SGDConfig.Builder sgdConfig = SGDConfig.newBuilder();
			sgdConfig.setLearningRate(_learningRate);
			sgdConfig.setL1Regularization(_l1Regularization);
			sgdConfig.setL2Regularization(_l2Regularization);
			config.setRegressionType(ModelConfig.RegressionType.LOGISTIC_REGRESSION);
			config.setExtension(SGDConfig.sgdConfig, sgdConfig.build());
			for (int i = 0; i < _num_classes; ++i) {
				_solvers.add(new SGDSolver());
				_solvers.get(i).Initialize(config.build());
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean TrainOnInstance(Instance inputInstance, int iteration) {
		Instance.Builder instance = Instance.newBuilder(inputInstance);
		for (int i = 0; i < _num_classes; ++i) {
			if(inputInstance.getLabel()==i) {
				instance.setLabel((float)1.0);
			} else {
				instance.setLabel((float)0.0);
			}
			_solvers.get(i).TrainOnInstance(instance.build(), iteration);
		}
		_modelUpdated = true;
		return true;
	}

	@Override
	public List<Float> PredictOnInstance(Instance instance) {
		List<Float> results = new ArrayList<Float>(_num_classes);
		for (int i = 0; i < _num_classes; ++i) {
			results.add(_solvers.get(i).PredictOnInstance(instance).get(0));
		}
		return results;
	}

	@Override
	public SerializedModel getModel() {
		if (_modelUpdated) {
			_model.clear();
			for(int i = 0; i < _num_classes; ++i) {
				MulticlassModel.ClassModel.Builder classModel =
						MulticlassModel.ClassModel.newBuilder();
				classModel.setLabel(i);
				classModel.setModel(_solvers.get(i).getModel().getLinearModel());
				_model.addClassModel(classModel.build());
			}
		}
		_modelUpdated = false;
		SerializedModel.Builder serializedModel = SerializedModel.newBuilder();
		serializedModel.setMulticlassModel(_model);
		return serializedModel.build();
	}

	@Override
	public boolean loadModel(InputStream input) {
		try {
			_model.mergeFrom(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
