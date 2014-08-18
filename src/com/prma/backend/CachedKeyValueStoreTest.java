package com.prma.backend;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prma.backend.*;

import org.junit.Assert;
import org.junit.Test;

public class CachedKeyValueStoreTest {
	@Test
	public void SimplePutGetTest() {
		KeyValueStore _remote = new MockKeyValueStore();
		CachedKeyValueStore _cache = new CachedKeyValueStore(_remote);
		// Insert remote pairs
		_remote.setKeyValue("key_a", "value_a");
		// Test remote fetch and cache.
		Assert.assertEquals("value_a", _cache.getValue("key_a"));
		Assert.assertTrue(_cache.getCache().containsKey("key_a"));
		// Test remote put
		Assert.assertTrue(_cache.setKeyValue("key_b", "value_b"));
		Assert.assertEquals("value_b", _remote.getValue("key_b"));
	}

	@Test
	public void BatchRemoteGetTest() {
		KeyValueStore _remote = new MockKeyValueStore();
		CachedKeyValueStore _cache = new CachedKeyValueStore(_remote);
		Map<String, String> remoteValues = new HashMap<String, String>();
		remoteValues.put("key_a", "value_a");
		remoteValues.put("key_b", "value_b");
		// Insert remote pairs
		Assert.assertTrue(_remote.batchSetKeyValues(remoteValues));
		// Test remote fetch and cache.
		List<String> localKeys = new ArrayList<String>();
		localKeys.add("key_a");
		localKeys.add("key_b");
		Map<String, String> localValues = _cache.batchGetValues(localKeys);
		Assert.assertEquals(remoteValues, localValues);

	}

	@Test
	public void BatchRemotePutTest() {
		KeyValueStore _remote = new MockKeyValueStore();
		CachedKeyValueStore _cache = new CachedKeyValueStore(_remote);
		Map<String, String> localValues = new HashMap<String, String>();
		localValues.put("key_a", "value_a");
		localValues.put("key_b", "value_b");
		// Insert remote pairs
		Assert.assertTrue(_cache.batchSetKeyValues(localValues));
		// Test remote fetch and cache.
		List<String> keys = new ArrayList<String>();
		keys.add("key_a");
		keys.add("key_b");
		Map<String, String> remoteValues = _remote.batchGetValues(keys);
		Assert.assertEquals(localValues, remoteValues);
	}

}
