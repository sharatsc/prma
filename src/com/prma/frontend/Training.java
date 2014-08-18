/**
 * 
 */
package com.prma.frontend;

import com.amazonaws.services.simpledb.model.Attribute;
import com.googlecode.protobuf.format.*;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.amazonaws.services.sqs.model.Message;
import com.prma.aws.AWSDB;
import com.prma.aws.AWSQueue;
import com.prma.API.TrainingRequest;
import com.prma.API.TrainingResponse;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
/**
 * @author sharat@prediction-machine.com
 *
 */
@Path("/training")
public class Training {
	final String TrainingTaskQueue = "TRAINING";
	AWSDB _db = new AWSDB();
	public Training() {
		AWSDB.createBucket();
	}
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String insertNewModel(String requestJson) {
		TrainingRequest.Builder request = TrainingRequest.newBuilder();
		TrainingResponse.Builder response = TrainingResponse.newBuilder();
		try {
			JsonFormat.merge(requestJson, request);
			AWSQueue queue = new AWSQueue();
			Message message = new Message();
			String modelId = UUID.randomUUID().toString();
			request.setModelId(modelId);
			message.setBody(JsonFormat.printToString(request.build()));
			queue.addMessage(message, AWSQueue.TrainingQueue);
			// Build response
			response.setUrl("/training/" + modelId);
			response.setModelId(modelId);
			response.setRequest(request);
			_db.setAttribute(request.getModelId(), "Status", "IN QUEUE");
		} catch (ParseException e) {
			response.setError("Invalid input format:" + requestJson);
		} catch (Exception e) {
			response.setError("Unable to train:" + requestJson);
		}
		return JsonFormat.printToString(response.build());
	}
	@GET
	@Path("/{model}") 
	@Produces(MediaType.TEXT_HTML)
	public String insertModel(@PathParam("model") String id) {
		List<Attribute> attributes = _db.getAllAttributes(id);
		StringBuilder output = new StringBuilder();
		output.append("<html><body><table>\n");
		for(int i = 0; i < attributes.size(); ++i) {
			output.append("<tr>\n");
			output.append("<td>" + attributes.get(i).getName() + "</td>\n");
			output.append("<td>" + attributes.get(i).getValue() + "</td>\n");
		}
		output.append("</table></body></html>\n");
		return output.toString();
	}
}
