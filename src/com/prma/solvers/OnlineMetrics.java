/**
 * 
 */
package com.prma.solvers;

import java.util.List;
import com.prma.Structures.*;

/**
 * @author sharat
 *
 */
public interface OnlineMetrics {
	public void resetMetrics();
	public void accumulateMetrics(Instance instance, List<Float> predictions);
	public float getFinalMetrics();
}
