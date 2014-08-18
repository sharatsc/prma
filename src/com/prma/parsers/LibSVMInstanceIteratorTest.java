package com.prma.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.prma.Structures.Instance;

public class LibSVMInstanceIteratorTest {
	StringBuilder _input = new StringBuilder();
	private InstanceIterator _parser = new LibSVMInstanceIterator();

	public StringBuilder getInput() {
		return _input;
	}
	public InputStream getInputStream() {
		return new ByteArrayInputStream(_input.toString().getBytes());
	}
	
	public InstanceIterator getParser() {
		return _parser;
	}
	@Test
	public void testInvalidInputMissingIndex() {
		getInput().append("-1 :-1.0");
		getParser().Initialize(getInputStream());
		assertFalse(getParser().hasNextInstance());
	}

	@Test
	public void testInvalidInputMissingValue() {
		getInput().append("-1 0:");
		getParser().Initialize(getInputStream());
		assertFalse(getParser().hasNextInstance());
	}

	@Test
	public void testInvalidInputFormatErrorInValue() {
		getInput().append("-1 0:a");
		getParser().Initialize(getInputStream());
		assertFalse(getParser().hasNextInstance());
	}
	
	@Test
	public void testInvalidInputFormatErrorInLabel() {
		getInput().append("-z 0:1.0");
		getParser().Initialize(getInputStream());
		assertFalse(getParser().hasNextInstance());
	}

	@Test
	public void testSimpleInput() {
		getInput().append("-1 0:0.1");
		getParser().Initialize(getInputStream());
		// Check instance availablility
		assertTrue(getParser().hasNextInstance());
		Instance instance = getParser().nextInstance();
		assertFalse(getParser().hasNextInstance());
		// Check label
		assertTrue(instance.getLabel() == -1);
		// Check attributes size
		assertTrue(instance.getAttributesCount() == 1);
		// Check attributes value
		assertEquals(instance.getAttributes(0).getIndex(), 0);
		assertEquals(instance.getAttributes(0).getValue(), 0.1, 0.01);
	}

	@Test
	public void testMultipleInstances() {
		getInput().append("-1 0:0.1 1:-0.1");
		getInput().append("\n");
		getInput().append("1 0:-0.1 1:0.1 2:0.2");
		getParser().Initialize(getInputStream());
		// Check instance availability
		assertTrue(getParser().hasNextInstance());
		Instance instanceA = getParser().nextInstance();
		assertTrue(getParser().hasNextInstance());
		Instance instanceB = getParser().nextInstance();
		assertFalse(getParser().hasNextInstance());
		// Check label
		assertEquals(instanceA.getLabel(),-1, 0.0001);
		assertEquals(instanceB.getLabel(), 1, 0.0001);
		// Check attributes size
		assertEquals(instanceA.getAttributesCount(), 2);
		assertEquals(instanceB.getAttributesCount(), 3);
	}
}