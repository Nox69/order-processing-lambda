package com.cts.mc.handler;

import static com.cts.mc.sqs.SQSPublishService.publishToSQS;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.cts.mc.model.Order;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 
 * @author Bharat Kumar
 *
 */
public class OrderProcessingLambda implements RequestHandler<SNSEvent, String> {

	private static final String SUCCESSFUL = "Products are successfully Ordered and published to Queue with details.";
	private static Logger log = LoggerFactory.getLogger(OrderProcessingLambda.class);

	@Override
	public String handleRequest(SNSEvent request, Context context) {

		// Start the Registration
		log.info("Order Processing started : [{}]", LocalDateTime.now());
		String orderDetails = request.getRecords().get(0).getSNS().getMessage();

		try {

			// Parse the message and add the Access Code
			Order order = retrieveOrders(orderDetails);
			log.info("Confirming Order for : [{}] with following products [{}]", order.getName(), order.getItems());

			// Process the message to SQS Queue
			publishToSQS(order);
			log.info("Successfully published the message");

		} catch (AmazonServiceException e) {
			log.error("Unable to process further due to sudden interruption");
		} catch (Exception e) {
			log.error("Exception Occurred while processing SNS Event : [{}] at [{}] with exception {}", orderDetails,
					LocalDateTime.now(), e);
		}
		return SUCCESSFUL;
	}

	private Order retrieveOrders(String orderDetails) {
		try {
			Gson gson = new Gson();
			log.info("URL Encoded JSON automatically Decoded : [{}]", orderDetails);
			return gson.fromJson(orderDetails, Order.class);
		} catch (JsonSyntaxException e) {
			log.error("Unable to Parse String to order Object.");
			throw new AmazonServiceException("Unable to Retrieve Order Details");
		}
	}

}
