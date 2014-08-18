/**
 * 
 */
package com.prma.solvers;
import java.io.InputStream;
import java.util.List;
import com.prma.Structures.*;

/**
 * @author sharat
 *
 */
public interface OnlineSolver {
	public boolean Initialize(ModelConfig configuration);
	public boolean TrainOnInstance(Instance instance, int iteration);
	public List<Float> PredictOnInstance(Instance instance);
	public SerializedModel getModel();
	public boolean loadModel(InputStream input);
}
