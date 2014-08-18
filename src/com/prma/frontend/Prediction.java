/**
 * 
 */
package com.prma.frontend;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.FormParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.sqs.model.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.prma.API.PredictionRequest;
import com.prma.API.PredictionResponse;
import com.prma.API.TrainingRequest;
import com.prma.API.TrainingResponse;
import com.prma.aws.AWSDB;
import com.prma.aws.AWSDynamo;
import com.prma.aws.AWSQueue;
import com.prma.aws.AWSStorage;
import com.prma.backend.KeyValueStore;


/** 
 * @author sharat
 *
 */
@Path("/prediction")
public class Prediction {
	KeyValueStore _db = new AWSDynamo();
	public Prediction() {
	}
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String insertNewModel(String requestJson) {
		PredictionRequest.Builder request = PredictionRequest.newBuilder();
		PredictionResponse.Builder response = PredictionResponse.newBuilder();
		try {
			JsonFormat.merge(requestJson, request);
		} catch (ParseException e) {
			//response.setError("Invalid input format:" + requestJson);
		} catch (Exception e) {
			//response.setError("Unable to train:" + requestJson);
		}
		return JsonFormat.printToString(response.build());
	}
}

