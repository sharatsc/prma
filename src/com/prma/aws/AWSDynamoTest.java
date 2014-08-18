package com.prma.aws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import com.prma.backend.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AWSDynamoTest {
	KeyValueStore _db = new AWSDynamo();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSimpleGetPut() {
		long start = System.currentTimeMillis();
		String randomValue = Double.toString(Math.random());
		Assert.assertTrue(_db.setKeyValue("_test" + randomValue, randomValue));
		Assert.assertEquals(_db.getValue("_test" + randomValue), randomValue);
		System.out.println("Time elapsed for simple set:" + (System.currentTimeMillis() - start));
	}
	
	@Test
	public void testBatchGetPut() {
		Map<String, String> map = new HashMap<String, String>();
		List<String> keys = new ArrayList<String>();
		for (int i = 0; i < 10; ++i) {
			String keyValue = Integer.toString(i);
			String randomValue = Double.toString(Math.random());
			map.put(keyValue, randomValue);
			keys.add(keyValue);
		}
		long start = System.currentTimeMillis();
		Assert.assertTrue(_db.batchSetKeyValues(map));
		System.out.println("Time elapsed for batchSet:" + (System.currentTimeMillis() - start));
		try {
			System.out.println("Sleeping to let values propagate");
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		start = System.currentTimeMillis();
		Map<String, String> resultMap = _db.batchGetValues(keys);
		System.out.println("Time elapsed for batchGet:" + (System.currentTimeMillis() - start));
		for (int i = 0; i < keys.size(); ++i) {
			String key = keys.get(i);
			Assert.assertEquals(map.get(key), resultMap.get(key));
		}
	}
}
