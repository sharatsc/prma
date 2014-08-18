/**
 * Streaming accuracy metrics. 
 */
package com.prma.solvers;

import java.util.List;

import com.prma.Structures.Instance;

/**
 * @author sharat
 *
 */
public class AccuracyMetrics implements OnlineMetrics {
	long _count = 0;
	long _correctLabels = 0;
	
	public int predictedClass(List<Float> predictions) {
		int maxIndex = -1;
		float maxValue = 0;
		if (predictions.size() == 1) {
			return predictions.get(0) > 0.5? 1: -1;
		}
		for (int i = 0; i < predictions.size(); ++i) {
			if (predictions.get(i) > maxValue) {
				maxValue = predictions.get(i);
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	@Override
	public void accumulateMetrics(Instance instance, List<Float> predictions) {
		// TODO Auto-generated method stub
		_count++;
		if (predictedClass(predictions) == instance.getLabel()) {
			_correctLabels++;
		}
	}

	@Override
	public float getFinalMetrics() {
		// TODO Auto-generated method stub
		return (float)_correctLabels/Math.max(_count, 1);
	}
	
	@Override
	public void resetMetrics() {
		_count = 0;
		_correctLabels = 0;
	}
}
