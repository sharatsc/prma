/**
 * 
 */
package com.prma.solvers;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import com.prma.parsers.*;

import com.prma.Solvers.SGDConfig;
import com.prma.Structures.Attribute;
import com.prma.Structures.GeneralizedLinearModel;
import com.prma.Structures.Instance;
import com.prma.Structures.ModelConfig;

/**
 * @author sharat
 * 
 */
class ProbabilisticInstanceParser implements InstanceIterator {
	float _probability = (float) 0.5;
	int _dimensions = 1;
	Random _random = new Random();

	ProbabilisticInstanceParser(
			float probability,	int dimensions) {
		_probability = probability;
		_dimensions = dimensions;
	}
	@Override
	public void Initialize(InputStream in) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasNextInstance() {
		return true;
	}

	@Override
	public Instance nextInstance() {
		Instance.Builder instance = Instance.newBuilder();
		Attribute.Builder attribute = null;
		for (int i =0; i < _dimensions; ++i) {
			attribute = Attribute.newBuilder();
			attribute.setIndex(i);
			attribute.setValue(1);
			instance.addAttributes(attribute);
		}
		instance.setLabel(0);
		if(Math.random() < _probability) {
			instance.setLabel(1);
		}
		return instance.build();
	}
}

/*
 * generates values according to y = ax + noise 
 */
class LinearInstanceParser implements InstanceIterator {
	float _a = (float) 0.5;
	int _dimensions = 1;

	LinearInstanceParser(float a, int dimensions) {
		_a = a;
		_dimensions = dimensions;
	}
	@Override
	public void Initialize(InputStream in) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasNextInstance() {
		return true;
	}

	@Override
	public Instance nextInstance() {
		Instance.Builder instance = Instance.newBuilder();
		float x = (float) Math.random();
		float y = (float) (_a *x + Math.random() * 0.01);
		Attribute.Builder attribute = null;
		for (int i = 0; i < _dimensions; ++i) {
			attribute =	Attribute.newBuilder()
					.setIndex(i)
					.setValue(x);
			instance.addAttributes(attribute.build());
		}
		instance.setLabel(y);
		return instance.build();
	}
}

public class SGDSolverTest {
	final float _probability = (float) 0.3;
	final float _learningRate = (float) 0.1;
	final float _l2Regularization = (float) 0.1;
	final float _l1Regularization = (float) 0.1;
	OnlineSolver _solver = new SGDSolver();
	ModelConfig.Builder _modelConfig = ModelConfig.newBuilder();
	SGDConfig.Builder _solverConfig = SGDConfig.newBuilder();
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		_solverConfig.setLearningRate(_learningRate);
		_solverConfig.setL1Regularization(_l1Regularization);
		_solverConfig.setL2Regularization(_l2Regularization);
		_modelConfig.setExtension(
				SGDConfig.sgdConfig,
				_solverConfig.build());
	}
	
	@Test
	public void testLogisticRegressionSingleDimension() {
		InstanceIterator _instanceParser = new ProbabilisticInstanceParser(_probability, 1);
		_modelConfig.setRegressionType(ModelConfig.RegressionType.LOGISTIC_REGRESSION);
		_solver.Initialize(_modelConfig.build());
		for(int i = 0; i < 50000; ++i) {
			Instance instance = _instanceParser.nextInstance();
			_solver.TrainOnInstance(instance, 0);
		}
		assertEquals(
				_solver.PredictOnInstance(_instanceParser.nextInstance()).get(0),
				_probability, 0.05);
	}

	@Test
	public void testLogisticRegressionMultipleDimension() {
		final int numDimensions = 10;
		InstanceIterator _instanceParser = new ProbabilisticInstanceParser(
				_probability, numDimensions);
		_modelConfig.setRegressionType(ModelConfig.RegressionType.LOGISTIC_REGRESSION);
		_solver.Initialize(_modelConfig.build());
		for(int i = 0; i < 50000; ++i) {
			Instance instance = _instanceParser.nextInstance();
			_solver.TrainOnInstance(instance, 0);
		}
		assertEquals(
				_solver.PredictOnInstance(_instanceParser.nextInstance()).get(0),
				_probability, 0.05);
		GeneralizedLinearModel model = _solver.getModel().getLinearModel();
		// Test convergence
		assertEquals(model.getWeightsCount(), numDimensions);
	}
	
	@Test
	public void testLinearRegressionSingleDimension() {
		// Generate y = 3x + noise
		final float coefficient = 3;
		InstanceIterator _instanceParser = new LinearInstanceParser(coefficient, 1);
		_modelConfig.setRegressionType(ModelConfig.RegressionType.LINEAR_REGRESSION);
		_solver.Initialize(_modelConfig.build());
		for(int i = 0; i < 50000; ++i) {
			Instance instance = _instanceParser.nextInstance();
			_solver.TrainOnInstance(instance, 0);
		}
		Instance instance = _instanceParser.nextInstance();
		assertEquals(
				_solver.PredictOnInstance(instance).get(0),
				instance.getAttributes(0).getValue() * coefficient,	0.05);

		GeneralizedLinearModel model = _solver.getModel().getLinearModel();
		// Test convergence
		assertEquals(model.getWeightsCount(), 1);
		assertEquals(model.getWeights(0).getValue(), coefficient, 0.05);
	}

	@Test
	public void testLinearRegressionMultipleDimensions() {
		// Generate y = 3x + noise
		final float coefficient = 12;
		final int numDimensions = 10;
		InstanceIterator _instanceParser = new LinearInstanceParser(coefficient, numDimensions);
		_modelConfig.setRegressionType(ModelConfig.RegressionType.LINEAR_REGRESSION);
		_solver.Initialize(_modelConfig.build());
		for(int i = 0; i < 500000; ++i) {
			Instance instance = _instanceParser.nextInstance();
			_solver.TrainOnInstance(instance, 0);
		}
		Instance instance = _instanceParser.nextInstance();
		assertEquals(
				_solver.PredictOnInstance(instance).get(0),
				instance.getAttributes(0).getValue() * coefficient,	0.05);

		GeneralizedLinearModel model = _solver.getModel().getLinearModel();
		// Test convergence
		assertEquals(model.getWeightsCount(), numDimensions);
		assertEquals(model.getWeights(0).getValue(), coefficient/numDimensions, 0.05);
	}

}
