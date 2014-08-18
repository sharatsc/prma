/**
 * 
 */
package com.prma.aws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

/**
 * @author sharat
 *
 */
public class AWSStorage {
	BasicAWSCredentials _credentials = new BasicAWSCredentials(AWSAccount.accessKey, AWSAccount.secretKey); 
	AmazonS3Client _s3 = new AmazonS3Client(_credentials);
	
	public InputStream getObject(String S3Path) throws URISyntaxException, IOException {
		URI uri = new URI(S3Path);
		String bucketName = uri.getHost();
		String key = uri.getPath().substring(1);
		S3Object object =  _s3.getObject(new GetObjectRequest(bucketName, key));
		if (object != null) {
			return object.getObjectContent();
		}
		return null;
	}
	
	public boolean putObject(InputStream input, String bucketName, String location) {
		PutObjectResult result = _s3.putObject(bucketName, location, input, new ObjectMetadata());
		return (result != null);
	}
}
