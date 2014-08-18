/**
 * 
 */
package com.prma.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.BatchGetItemRequest;
import com.amazonaws.services.dynamodb.model.BatchGetItemResult;
import com.amazonaws.services.dynamodb.model.BatchResponse;
import com.amazonaws.services.dynamodb.model.GetItemRequest;
import com.amazonaws.services.dynamodb.model.GetItemResult;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.KeysAndAttributes;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.PutItemResult;
import com.prma.backend.*;
/**
 * @author sharat
 *
 */
public class AWSDynamo implements KeyValueStore{
	static BasicAWSCredentials _credentials = new BasicAWSCredentials(AWSAccount.accessKey, AWSAccount.secretKey);
	static AmazonDynamoDBAsyncClient _db = new AmazonDynamoDBAsyncClient(_credentials);
	public static final String ModelsDB = "models";

	private Map<String, AttributeValue> newItem(String key, String value) {
		Map<String, AttributeValue> entries = new HashMap<String, AttributeValue>();
		entries.put("attribute", new AttributeValue(key));
		entries.put("value", new AttributeValue(value));
		return entries;
	}
	
	@Override
	public boolean setKeyValue(String key, String value) {
		PutItemRequest request = new PutItemRequest();
		request.setRequestCredentials(_credentials);
		request.setTableName(ModelsDB);
		request.setItem(newItem(key, value));
		try {
			PutItemResult result = _db.putItem(request);
			return (result != null);
		} catch (Exception e) {
			System.err.println("Error while batchSet");
			return false;
		}
	}

	@Override
	public boolean batchSetKeyValues(Map<String, String> map) {

		for (Map.Entry<String, String> entry: map.entrySet()) {
			PutItemRequest request = new PutItemRequest();
			request.setRequestCredentials(_credentials);
			request.setTableName(ModelsDB);
			request.setItem(newItem(entry.getKey(), entry.getValue()));
			try {
				Future<PutItemResult> result = _db.putItemAsync(request);
				if (result == null) {
					return false;
				}
			} catch (Exception e) {
				System.err.println("Error while batchSet");
				return false;
			}
		}
		return true;
	}

	@Override
	public String getValue(String key) {
		GetItemRequest request = new GetItemRequest();
		request.setTableName(ModelsDB);
		request.setConsistentRead(true);
		request.setRequestCredentials(_credentials);
		request.setKey(new Key(new AttributeValue(key)));
		List<String> attributes = new ArrayList<String>();
		attributes.add("value");
		request.setAttributesToGet(attributes);
		GetItemResult result = _db.getItem(request);
		if (result == null) {
			return null;
		}
		Map<String, AttributeValue> returnValue = result.getItem();
		if (returnValue == null ) {
			return "";
		}
		return returnValue.get("value").getS();
	}

	@Override
	public Map<String, String> batchGetValues(List<String> keys) {
		Map<String, String> result = new HashMap<String, String>();
		int numBatches = (keys.size() + 99)/100;

		List<String> attributesToGet = new ArrayList<String>();
		attributesToGet.add("attribute");
		attributesToGet.add("value");
		List<Future<BatchGetItemResult>> futureResult = new ArrayList<Future<BatchGetItemResult>>();
		
		for (int i = 0; i < numBatches; ++i) {
			Map<String, KeysAndAttributes> requestItems = new HashMap<String, KeysAndAttributes>();
			
			List<Key> keysToGet = new ArrayList<Key>();
			for (int j = i*100; j < keys.size() && j < (i+1) * 100; ++j) {
				keysToGet.add(new Key(new AttributeValue(keys.get(j))));
			}
			KeysAndAttributes keysAndAttributesToGet = new KeysAndAttributes();
			keysAndAttributesToGet.setAttributesToGet(attributesToGet);
			keysAndAttributesToGet.setKeys(keysToGet);
			
			requestItems.put(ModelsDB, keysAndAttributesToGet);
			BatchGetItemRequest batchGetItemRequest = new BatchGetItemRequest().withRequestItems(requestItems);
			futureResult.add(_db.batchGetItemAsync(batchGetItemRequest));
		}
		
		for (Future<BatchGetItemResult> batchGetItemResult: futureResult){
			try {
				Collection<BatchResponse> batchResponse = null;
				batchResponse = batchGetItemResult.get().getResponses().values();
				Iterator<BatchResponse> responseIterator = batchResponse.iterator();
				List<Map<String, AttributeValue>> responseList = responseIterator.next().getItems();
				for (Map<String, AttributeValue> response: responseList) {
					result.put(response.get("attribute").getS(), response.get("value").getS());
				}
			} catch (Exception e) {
				System.err.println("Unable to get keys.");
			}
		}
		return result;
	}

	@Override
	public boolean setAttributeValue(String key, String attribute, String value) {
		// TODO Auto-generated method stub
		return false;
	}

}
