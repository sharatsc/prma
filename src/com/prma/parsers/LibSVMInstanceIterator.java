package com.prma.parsers;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.prma.Structures.Attribute;
import com.prma.Structures.Instance;

public class LibSVMInstanceIterator implements InstanceIterator{
	Instance _currentInstance = null;
	boolean _hasNextInstance = false;
	BufferedReader _reader = null;
	@Override
	public void Initialize(InputStream in) {
		_reader = new BufferedReader(new InputStreamReader(in));
		_hasNextInstance = parseNextInstance();
	}

	@Override
	public boolean hasNextInstance() {
		return _hasNextInstance;
	}

	@Override
	public Instance nextInstance() {
		if(_hasNextInstance) {
			Instance instance = _currentInstance;
			_hasNextInstance = parseNextInstance();
			return instance;
		}
		return null;
	}

	private boolean parseNextInstance() {
		Instance.Builder instance = Instance.newBuilder();
		try {
			String line = _reader.readLine();
			if (line == null) {
				return false;
			}
			String[] entries = line.split("\\s");
			if(entries.length < 1) {
				return false;
			}
			instance.setLabel(Float.parseFloat(entries[0]));
			for(int i = 1; i < entries.length; ++i) {
				Attribute.Builder attribute = Attribute.newBuilder();
				String [] indexValuePair = entries[i].split(":");
				if(indexValuePair.length != 2) {
					return false;
				}
				attribute.setIndex(Integer.parseInt(indexValuePair[0]));
				attribute.setValue(Float.parseFloat(indexValuePair[1]));
				instance.addAttributes(attribute);
			}
			_currentInstance = instance.build();
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
}
