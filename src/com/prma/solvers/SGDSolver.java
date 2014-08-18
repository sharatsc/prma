/**
 * 
 */
package com.prma.solvers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;

import com.prma.Solvers.SGDConfig;
import com.prma.Structures.GeneralizedLinearModel;
import com.prma.Structures.SerializedModel;
import com.prma.Structures.Instance;
import com.prma.Structures.Attribute;
import com.prma.Structures.ModelConfig;
import com.prma.Structures.Weight;

/**
 * @author sharat
 *
 */

class Coefficient {
	float _coefficient = 0;
	float _totalGradient = 0;
	long _totalCount = 0;
	public float sign(float x) {
		if (x < 0) {
			return -1;
		}
		return 1;
	}
	public float getCoefficient() {
		return _coefficient;
	}
	public void updateCoefficient(float gradient, 
								  int iteration,
								  int totalCount,
								  float learningRate,
								  float l1Regularization,
								  float l2Regularization) {
		++_totalCount;
		_totalGradient += gradient * gradient;
		learningRate = (float) (learningRate/Math.max(1, Math.sqrt(_totalGradient)));
		_coefficient -= learningRate * gradient;
		_coefficient /= (1.0 + 2 * l2Regularization * learningRate / _totalCount);
	}
}

public class SGDSolver implements OnlineSolver{
	boolean _modelUpdated = false;
	Map<Integer, Coefficient> _map = new HashMap<Integer, Coefficient>();
	Coefficient _bias = new Coefficient();
	
	GeneralizedLinearModel.Builder _model = GeneralizedLinearModel.newBuilder();
	float _learningRate = (float) 0.1;
	float _l2Regularization = (float) 0.1;
	float _l1Regularization = (float) 0.0;
	int _totalCount = 0;
	ModelConfig.RegressionType _regressionType = (
			ModelConfig.RegressionType.LOGISTIC_REGRESSION);

	public SGDSolver() {
	}

	@Override
	public boolean TrainOnInstance(Instance instance, int iteration) {
		_totalCount++;
		List<Float> predictions = PredictOnInstance(instance); 
		float error =  predictions.get(0)- instance.getLabel();
		_bias.updateCoefficient(error, iteration, _totalCount, _learningRate, 0, 0);
		for(int i = 0; i < instance.getAttributesCount(); ++i) {
			Integer key = new Integer(instance.getAttributes(i).getIndex());
			Coefficient coefficient = _map.get(key);
			if(coefficient == null) {
				coefficient = new Coefficient();
				_map.put(key, coefficient);
			}
			coefficient.updateCoefficient(
					error * instance.getAttributes(i).getValue(),
					iteration,
					_totalCount,
					_learningRate, _l1Regularization, _l2Regularization);
		}
		_modelUpdated = true;
		return true;
	}

	@Override
	public List<Float> PredictOnInstance(Instance instance) {
		//TODO: Predict based on regression type.
		float dotProduct = _bias.getCoefficient();
		for (int i = 0; i < instance.getAttributesCount(); ++i) {
			Attribute attribute = instance.getAttributes(i);
			if (_map.containsKey(attribute.getIndex())) {
				Coefficient coefficient = _map.get(attribute.getIndex());
				dotProduct += coefficient.getCoefficient() * attribute.getValue();
			}
		}
		float returnValue = 0;
		switch(_regressionType) {
			case LOGISTIC_REGRESSION:
				returnValue = (float)(1.0/(1.0 + Math.exp(-dotProduct)));
			case LINEAR_REGRESSION:
				returnValue = (float)dotProduct;
		}
		List<Float> ret = new ArrayList<Float>(1);
		ret.add(returnValue);
		return ret;
	}

	@Override
	public SerializedModel getModel() {
		SerializedModel.Builder serializedModel = SerializedModel.newBuilder();
		if (_modelUpdated) {
			_model.clearWeights();
			_model.setBias(Weight.newBuilder()
					.setIndex(0)
					.setValue(_bias.getCoefficient()));
			for(Entry<Integer,Coefficient> entry: _map.entrySet()) {
				Weight.Builder weight = Weight.newBuilder();
				weight.setIndex(entry.getKey());
				weight.setValue(entry.getValue().getCoefficient());
				_model.addWeights(weight);
			}
		}
		_modelUpdated = false;
		serializedModel.setLinearModel(_model);
		return serializedModel.build();
	}
	
	@Override
	public boolean Initialize(ModelConfig config) {
		_totalCount = 0;
		_regressionType = config.getRegressionType(); 
		if (config.hasExtension(
				SGDConfig.sgdConfig)) {
			SGDConfig solverConfig =
					config.getExtension(
							SGDConfig.sgdConfig);
			_learningRate = solverConfig.getLearningRate();
			_l2Regularization = solverConfig.getL2Regularization();
			_l1Regularization = solverConfig.getL1Regularization();
		}
		return true;
	}

	@Override
	public boolean loadModel(InputStream input) {
		SerializedModel.Builder serializedModel = SerializedModel.newBuilder();
		_model.clearWeights();
		try {
			serializedModel.mergeFrom(input);
			_model.mergeFrom(serializedModel.getLinearModel());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
}
