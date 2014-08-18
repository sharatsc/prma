package com.prma.aws;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

/**
 * @author sharat
 *
 */
public class AWSQueue {
	public static final String TrainingQueue = "https://queue.amazonaws.com/112632332074/TRAINING";
	BasicAWSCredentials _credentials = new BasicAWSCredentials(AWSAccount.accessKey, AWSAccount.secretKey); 
	AmazonSQS _sqs = new AmazonSQSClient(_credentials);
	public boolean createQueue(String name) throws AmazonServiceException{
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(name);
        String myQueueUrl = _sqs.createQueue(createQueueRequest).getQueueUrl();
        System.out.println("Created a new SQS queue@" + myQueueUrl);
        return true;
	}

	public SendMessageResult addMessage(Message message, String queueUrl) {
		SendMessageRequest sendRequest = new SendMessageRequest();
		sendRequest.setRequestCredentials(_credentials);
		sendRequest.setQueueUrl(queueUrl);
		sendRequest.setMessageBody(message.getBody());
		return _sqs.sendMessage(sendRequest);
	}
	
	public Message getNextMessage(String queueUrl, int timeOut) {
		ReceiveMessageRequest request = new ReceiveMessageRequest().
				withMaxNumberOfMessages(1).
				withQueueUrl(queueUrl);
		request.setRequestCredentials(_credentials);
		request.setVisibilityTimeout(timeOut);
		ReceiveMessageResult result = _sqs.receiveMessage(request);
		if(!result.getMessages().isEmpty()) {
			Message message = result.getMessages().get(0);
			System.out.println("Message body:"+ message.getBody());
			return message;
		} else {
			return null;
		}
	}
	
	public boolean deleteMessage(Message message) {
		DeleteMessageRequest request = new DeleteMessageRequest();
		request.setRequestCredentials(_credentials);
		request.setQueueUrl(TrainingQueue);
		request.setReceiptHandle(message.getReceiptHandle());
		_sqs.deleteMessage(request);
		return true;
	}
}
