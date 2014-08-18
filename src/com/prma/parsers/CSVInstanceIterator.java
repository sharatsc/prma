/**
 * 
 */
package com.prma.parsers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.prma.Structures.Attribute;
import com.prma.Structures.Instance;

import java.util.regex.Pattern;
import com.prma.Constants;
/**
 * @author sharat
 *
 */
public class CSVInstanceIterator implements InstanceIterator {
	Instance _currentInstance = null;
	boolean _hasNextInstance = false;
	//
	Pattern _numericRegex = Pattern.compile("^[-+]?[0-9]*.?[0-9]+([eE][-+]?[0-9]+)?$");
	BufferedReader _reader = null;

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
			String[] entries = line.split(",");
			if(entries.length < 2) { // need label and at least one attribute
				return false;
			}
			instance.setLabel(Float.parseFloat(entries[0]));
			for(int i = 1; i < entries.length; ++i) {
				Attribute.Builder attribute = Attribute.newBuilder();
				if(_numericRegex.matcher(entries[i]).matches()) {
					attribute.setIndex(i-1);
					attribute.setValue(Float.parseFloat(entries[i]));
				} else {
					String token;
					int len = entries[i].length();
					if ((entries[i].charAt(0) == '"' && entries[i].charAt(len - 1) == '"') ||
						(entries[i].charAt(0) == '\'' && entries[i].charAt(len - 1) == '\'')) {
						token = entries[i].substring(1, len - 1);
					} else {
						token = entries[i];
					}
					int hashCode = (token + i).hashCode() % Constants.MAX_INDEX;
					attribute.setIndex(hashCode);
					attribute.setValue(1.0f);
				}
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
