package com.prma.solvers;

import java.util.List;

import com.prma.Structures.Instance;

public class MSEMetrics implements OnlineMetrics {
	double _error = 0;
	long _count = 0;
	
	@Override
	public void resetMetrics() {
		_error = 0;
		_count = 0;
	}

	@Override
	public void accumulateMetrics(Instance instance, List<Float> predictions) {
		_error += Math.pow(predictions.get(0) - instance.getLabel(), 2);
		_count ++;
	}

	@Override
	public float getFinalMetrics() {
		return (float)Math.sqrt(_error/Math.max(_count, 1));
	}

}
