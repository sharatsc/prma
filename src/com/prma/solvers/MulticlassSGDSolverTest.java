package com.prma.solvers;

import java.io.InputStream;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.prma.Solvers.MultiSGDConfig;
import com.prma.Structures.Attribute;
import com.prma.Structures.Instance;
import com.prma.Structures.ModelConfig;
import com.prma.parsers.InstanceIterator;

/**
 * @author sharat
 * 
 */
class MockMulticlassInstanceParser implements InstanceIterator {
	float[] _cum = null;
	int _dimensions = 1;
	int _num_classes = 1;
	Random _random = new Random();

	MockMulticlassInstanceParser(
			float[] probabilities, int dimensions) {
		_cum = new float[probabilities.length];
		_num_classes = probabilities.length;
		_dimensions = dimensions;
		_cum[0] = probabilities[0];
		for (int i = 1; i < _num_classes; ++i) {
			_cum[i] = _cum[i-1] + probabilities[i];
		}
		Assert.assertEquals(_cum[_num_classes -1], 1.0f, 1e-3);
	}
	@Override
	public void Initialize(InputStream in) {
	}

	@Override
	public boolean hasNextInstance() {
		return true;
	}

	@Override
	public Instance nextInstance() {
		Instance.Builder instance = Instance.newBuilder();
		Attribute.Builder attribute = null;
		float rnd = (float)Math.random();
		for (int i = 0; i < _num_classes; ++i) {
			if (rnd <= _cum[i] ) {
				instance.setLabel(i);
				break;
			}
		} 
		for (int i =0; i < _dimensions; ++i) {
			attribute = Attribute.newBuilder();
			attribute.setIndex(_dimensions * _num_classes + i).setValue(1);
			instance.addAttributes(attribute);
		}
		return instance.build();
	}
}

public class MulticlassSGDSolverTest {
	final float[] _probabilities = { 0.1f, 0.2f, 0.3f, 0.4f };
	final int _num_classes = _probabilities.length;
	final float _learningRate = (float) 0.1;
	final float _l2Regularization = (float) 10.0;
	final float _l1Regularization = (float) 10.0;
	OnlineSolver _solver = new MulticlassSGDSolver();
	ModelConfig.Builder _modelConfig = ModelConfig.newBuilder();
	MultiSGDConfig.Builder _solverConfig = MultiSGDConfig.newBuilder();
	
	public Instance nextBiasedInstance(int label, int dimensions) {
		Instance.Builder instance = Instance.newBuilder();
		Attribute.Builder attribute = null;
		instance.setLabel(label);
		for (int i =0; i < dimensions; ++i) {
			attribute = Attribute.newBuilder();
			attribute.setIndex(dimensions * label + i);
			attribute.setValue(1);
			instance.addAttributes(attribute);
		}
		return instance.build();
	}
	
	public int predictedClass(List<Float> predictions) {
		int maxIndex = -1;
		float maxValue = 0;
		for (int i = 0; i < predictions.size(); ++i) {
			if (predictions.get(i) > maxValue) {
				maxValue = predictions.get(i);
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		_solverConfig.setLearningRate(_learningRate);
		_solverConfig.setL1Regularization(_l1Regularization);
		_solverConfig.setL2Regularization(_l2Regularization);
		_solverConfig.setNumClasses(_probabilities.length);
		_modelConfig.setExtension(
				MultiSGDConfig.multiSgdConfig,
				_solverConfig.build());
	}

	@Test
	// Velociraptor asks "Who will test the tests"?
	public void testMockIterator() {
		float[] probabilities = {0.1f, 0.2f, 0.3f, 0.4f, 0.0f};
		long [] counts = {0, 0, 0, 0, 0};
		MockMulticlassInstanceParser parser =
				new MockMulticlassInstanceParser(
						probabilities, 5);
		final int trials = 1000;
		for (int i = 0; i < trials; ++i) {
			Instance instance = parser.nextInstance();
			counts[(int)(instance.getLabel())]++;
		}
		for(int i =0; i < counts.length; ++i ) {
			float sigma = (float) Math.sqrt(trials * probabilities[i] * (1-probabilities[i]));
			Assert.assertEquals(counts[i], trials * probabilities[i], 3 * sigma);
		}
	}
	
	@Test
	public void testMulticlassSingleDimension() {
		InstanceIterator _instanceParser = new MockMulticlassInstanceParser(_probabilities, 1);
		_solver.Initialize(_modelConfig.build());
		for(int i = 0; i < 10000; ++i) {
			Instance instance = _instanceParser.nextInstance();
			_solver.TrainOnInstance(instance, 0);
		}
		Instance instance = _instanceParser.nextInstance();
		List<Float> predictions = _solver.PredictOnInstance(instance);
		for(int i = 0; i < _num_classes; ++i) {
			Assert.assertEquals(predictions.get(i), _probabilities[i], 0.1);
		}
	}
	@Test
	public void testMulticlassMultiDimension() {
		InstanceIterator _instanceParser = new MockMulticlassInstanceParser(_probabilities, 5);
		_solver.Initialize(_modelConfig.build());
		for(int i = 0; i < 10000; ++i) {
			Instance instance = _instanceParser.nextInstance();
			_solver.TrainOnInstance(instance, 0);
		}
		// Check if it has converged.
		for(int i = 0; i < _num_classes; ++i) {
			Instance instance = _instanceParser.nextInstance();
			List<Float> predictions = _solver.PredictOnInstance(instance);
			Assert.assertEquals(predictions.get(i), _probabilities[i], 0.15);
		}
	}
	
	@Test
	public void testMultiDimensionPrediction() {
		final int kDimensions = 10;
		_solver.Initialize(_modelConfig.build());
		for(int i = 0; i < 10000; ++i) {
			Instance instance = nextBiasedInstance(i % _num_classes, kDimensions);
			_solver.TrainOnInstance(instance, 0);
		}
		// Check if it has converged.
		for(int i = 0; i < _num_classes; ++i) {
			Instance instance = nextBiasedInstance(i, kDimensions);
			List<Float> predictions = _solver.PredictOnInstance(instance);
			System.out.println(predictions);
			Assert.assertTrue(predictedClass(predictions) == i);
		}
	}
}
