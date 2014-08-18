/**
 * 
 */
package com.prma.parsers;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

import com.prma.Structures.Instance;

/**
 * @author sharat
 *
 */
public class ChainedInstanceIterator implements InstanceIterator {
	Queue<InstanceIterator> _iterators = new LinkedList<InstanceIterator>();
	InstanceIterator _currentIterator = null;

	public void addIterator(InstanceIterator iterator) {
		_iterators.add(iterator);
		if (_currentIterator == null) {
			_currentIterator = iterator;
		}
	}

	public ChainedInstanceIterator() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public void Initialize(InputStream in) {
		
	}
	@Override
	public boolean hasNextInstance() {
		if (_currentIterator == null) {
			return false;
		}
		if (!_currentIterator.hasNextInstance()) {
			_iterators.remove();
			if (_iterators.size() == 0) {
				return false;
			}
			_currentIterator = _iterators.peek();
			return _currentIterator.hasNextInstance();
		}
		return true;
	}
	@Override
	public Instance nextInstance() {
		if (_currentIterator == null) {
			return null;
		}
		return _currentIterator.nextInstance();
	}
}
