package com.prma.aws;

import java.util.ArrayList;
import java.util.List;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.Attribute;

public class AWSDB {
	public static final String ModelsDB = "models";
	static BasicAWSCredentials _credentials = new BasicAWSCredentials(AWSAccount.accessKey, AWSAccount.secretKey);
	static AmazonSimpleDBClient _db = new AmazonSimpleDBClient(_credentials);
	
	public static boolean createBucket() {
		CreateDomainRequest request = new CreateDomainRequest();
		request.setDomainName(ModelsDB);
		request.setRequestCredentials(_credentials);
		_db.createDomain(request);
		return true;
	}
	
	public boolean setAttribute(String modelName, String attribute, String value) {
		PutAttributesRequest request = new PutAttributesRequest();
		request.setDomainName(ModelsDB);
		request.setItemName(modelName);
		List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
		attributes.add(new ReplaceableAttribute(attribute, value, true));
		request.setAttributes(attributes);
		_db.putAttributes(request);
		return true;
	}
	
	public String getAttribute(String modelName, String attribute) {
		GetAttributesRequest request = new GetAttributesRequest().
				withAttributeNames(attribute).withDomainName(ModelsDB).withItemName(modelName);
		request.setRequestCredentials(_credentials);
		GetAttributesResult response = _db.getAttributes(request);
		List<Attribute> attributes = response.getAttributes();
		if(attributes.size() == 1) {
			return attributes.get(0).getValue();
		} else {
			return null;
		}
	}
	
	public List<Attribute> getAllAttributes(String modelName) {
		GetAttributesRequest request = new GetAttributesRequest().
				withDomainName(ModelsDB).withItemName(modelName);
		request.setRequestCredentials(_credentials);
		GetAttributesResult response = _db.getAttributes(request);
		List<Attribute> attributes = response.getAttributes();
		return attributes;
	}
}
