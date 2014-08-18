package com.prma.backend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockKeyValueStore implements KeyValueStore{
	Map<String, String> _map = new HashMap<String, String>();
	
	@Override
	public boolean setKeyValue(String key, String value) {
		_map.put(key, value);
		return true;
	}

	@Override
	public boolean setAttributeValue(String key, String attribute, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean batchSetKeyValues(Map<String, String> map) {
		for(Map.Entry<String, String> entry : map.entrySet()) {
			_map.put(entry.getKey(), entry.getValue());
		}
		return true;
	}

	@Override
	public String getValue(String key) {
		return _map.get(key);
	}

	@Override
	public Map<String, String> batchGetValues(List<String> keys) {
		Map<String,String> result = new HashMap<String, String>();
		for (String key: keys) {
			result.put(key, _map.get(key));
		}
		return result;
	}

}
