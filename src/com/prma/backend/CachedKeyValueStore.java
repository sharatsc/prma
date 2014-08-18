/**
 * 
 */
package com.prma.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * @author sharat
 *
 */
public class CachedKeyValueStore implements KeyValueStore{
	KeyValueStore _remoteStore = null;
	Map<String, String> _cache = new LinkedHashMap<String, String>(1<<24, 0.75f, true);
	ExecutorService _executor = Executors.newFixedThreadPool(2);
	
	public CachedKeyValueStore(KeyValueStore remoteStore) {
		_remoteStore = remoteStore;
	}

	public Map<String,String> getCache() {
		return _cache;
	}
	
	@Override
	public boolean setKeyValue(String key, String value) {
		_cache.put(key,  value);
		return _remoteStore.setKeyValue(key, value);
	}

	@Override
	public boolean setAttributeValue(String key, String attribute, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean batchSetKeyValues(Map<String, String> map) {
		for (Map.Entry<String, String> entry: map.entrySet()) {
			_cache.put(entry.getKey(), entry.getValue());
		}
		_remoteStore.batchSetKeyValues(map);
		return true;
	}

	@Override
	public String getValue(String key) {
		if (_cache.containsKey(key)) {
			return _cache.get(key);
		}
		String value =  _remoteStore.getValue(key);
		_cache.put(key, value);
		return value;
	}

	@Override
	public Map<String, String> batchGetValues(List<String> keys) {
		Map<String, String> result = new HashMap<String, String>();
		List<String> remoteKeys = new ArrayList<String>();
		List<String> localKeys = new ArrayList<String>();
		for(String key: keys) {
			if (_cache.containsKey(key)) {
				localKeys.add(key);
			} else {
				remoteKeys.add(key);
			}
		}
		if (remoteKeys.size() > 0) {
			result = _remoteStore.batchGetValues(remoteKeys);
		}
		for (String key: localKeys) {
			result.put(key, _cache.get(key));
		}
		// Fill cache with remote results.
		for (String key: remoteKeys) {
			_cache.put(key, result.get(key));
		}
		return result;
	}
}
