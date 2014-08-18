package com.prma.parsers;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.prma.Structures.Instance;

public class StringInstanceIteratorTest {
	StringBuilder _input = new StringBuilder();
	private InstanceIterator _parser = new StringInstanceIterator();

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
	public void testInvalidInputMissingLabel() {
		getInput().append("hello");
		getParser().Initialize(getInputStream());
		assertFalse(getParser().hasNextInstance());
	}
	
	@Test
	public void testInvalidInputFormatErrorInLabel() {
		getInput().append("-1 1");
		getParser().Initialize(getInputStream());
		assertFalse(getParser().hasNextInstance());
	}
	
	@Test
	public void testSimpleInput() {
		getInput().append("1 hello world");
		getParser().Initialize(getInputStream());
		// Check instance availablility
		assertTrue(getParser().hasNextInstance());
		Instance instance = getParser().nextInstance();
		assertFalse(getParser().hasNextInstance());
		// Check label
		assertTrue(instance.getLabel() == 1);
		// Check attributes size
		assertTrue(instance.getAttributesCount() == 2);
		// Check attributes value
		assertEquals(instance.getAttributes(0).getIndex(), "hello".hashCode()%(1<<16));
		assertEquals(instance.getAttributes(0).getValue(), 1.0f, 1e-3);

		assertEquals(instance.getAttributes(1).getIndex(), "world".hashCode()%(1<<16));
		assertEquals(instance.getAttributes(1).getValue(), 1.0f, 1e-3);
	}

	@Test
	public void testMultipleInstances() {
		getInput().append("0 bad sentiment altogether.");
		getInput().append("\n");
		getInput().append("1 good sentiment");
		getParser().Initialize(getInputStream());
		// Check instance availability
		assertTrue(getParser().hasNextInstance());
		Instance instanceA = getParser().nextInstance();
		assertTrue(getParser().hasNextInstance());
		Instance instanceB = getParser().nextInstance();
		assertFalse(getParser().hasNextInstance());
		// Check label
		assertEquals(instanceA.getLabel(), 0, 0.0001);
		assertEquals(instanceB.getLabel(), 1, 0.0001);
		// Check attributes size
		assertEquals(instanceA.getAttributesCount(), 3);
		assertEquals(instanceB.getAttributesCount(), 2);
	}

}
