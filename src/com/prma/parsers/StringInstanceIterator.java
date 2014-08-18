/**
 * 
 */
package com.prma.parsers;
import com.prma.Constants;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.prma.Structures.Attribute;
import com.prma.Structures.Instance;

/**
 * @author sharat 
 *
 */
public class StringInstanceIterator implements InstanceIterator {
	Instance _currentInstance = null;
	boolean _hasNextInstance = false;
	BufferedReader _reader = null;
	final int _maxIndex = Constants.MAX_INDEX; 
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
			if(entries.length < 2) { // need label and at least one attribute
				return false;
			}
			float label = Float.parseFloat(entries[0]);
			if (label < 0) {
				return false;
			}
			instance.setLabel(label);
			for(int i = 1; i < entries.length; ++i) {
				Attribute.Builder attribute = Attribute.newBuilder();
				// TODO(sharat): Make maxIndex a config of the iterator
				int index = entries[i].hashCode()%_maxIndex;
				attribute.setIndex(index);
				attribute.setValue(1.0f);
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