/**
 * 
 */
package com.prma.backend;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author sharat
 *
 */
public interface KeyValueStore {
	public boolean setKeyValue(String key, String value);
	public boolean setAttributeValue(String key, String attribute, String value);
	public boolean batchSetKeyValues(Map<String,String> map);
	public String getValue(String key);
	public Map<String, String> batchGetValues(List<String> keys);
}
