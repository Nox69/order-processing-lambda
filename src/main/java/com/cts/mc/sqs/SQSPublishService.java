package com.cts.mc.sqs;

import static com.cts.mc.config.AwsClientConfiguration.sqsClient;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.cts.mc.model.Order;
import com.cts.mc.model.Product;

/**
 * @author bharatkumar
 *
 */
public class SQSPublishService {

	private SQSPublishService() {
		// Utility classes should not have public constructors (squid:S1118)
	}

	private static final String TYPE_ATTRIBUTE = "type";
	private static final String EMAIL_TYPE = "process-order";
	private static final String EMAIL_ATTRIBUTE = "email";
	private static final String FIRST_NAME_ATTRIBUTE = "firstName";
	private static final String ORDER_CONFIRMATION_ATTRIBUTE = "code";
	private static final String ORDER_ID_ATTRIBUTE = "orderId";
	private static final String ATTRIBUTE_DATATYPE = "String";
	private static final String SPACE = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	private static final String LINE_BREAK = "<br>";

	private static Logger log = LoggerFactory.getLogger(SQSPublishService.class);
	private static final String SQS_QUEUE_URL = "https://sqs.us-east-2.amazonaws.com/960560987724/order-processing-queue";

	public static void publishToSQS(Order order) {

		log.info("Creating the message Request to be published to Queue.");
		SendMessageRequest messageRequest = new SendMessageRequest().withQueueUrl(SQS_QUEUE_URL)
				.withMessageAttributes(fillMessageAttributes(order)).withDelaySeconds(5)
				.withMessageBody(formatBody(order.getItems()));

		// publish the message with SQS Client
		sqsClient().sendMessage(messageRequest);
	}

	private static Map<String, MessageAttributeValue> fillMessageAttributes(Order order) {
		MessageAttributeValue typeAttrVal = new MessageAttributeValue().withStringValue(EMAIL_TYPE)
				.withDataType(ATTRIBUTE_DATATYPE);
		MessageAttributeValue emailAttrVal = new MessageAttributeValue().withStringValue(order.getEmailId())
				.withDataType(ATTRIBUTE_DATATYPE);
		MessageAttributeValue firstNameAttrVal = new MessageAttributeValue().withStringValue(order.getName())
				.withDataType(ATTRIBUTE_DATATYPE);
		MessageAttributeValue orderIdAttrVal = new MessageAttributeValue().withStringValue(order.getOrderCode())
				.withDataType(ATTRIBUTE_DATATYPE);

		// Generating the Order Confirmation Code to be sent in mail So User can accept
		// it.
		String orderConfirmationCode = getOrderConfirmationCode(order.getOrderCode());

		if (orderConfirmationCode == null) {
			log.info("Stopping the order processing as Confirmation code cannot be generated");
			throw new AmazonServiceException("Unable to process further");
		}

		MessageAttributeValue orderConfirmationAttrVal = new MessageAttributeValue()
				.withStringValue(orderConfirmationCode).withDataType(ATTRIBUTE_DATATYPE);

		Object[][] messageAttributesMap = new Object[][] { { TYPE_ATTRIBUTE, typeAttrVal },
				{ EMAIL_ATTRIBUTE, emailAttrVal }, { FIRST_NAME_ATTRIBUTE, firstNameAttrVal },
				{ ORDER_CONFIRMATION_ATTRIBUTE, orderConfirmationAttrVal }, { ORDER_ID_ATTRIBUTE, orderIdAttrVal } };

		return Stream.of(messageAttributesMap)
				.collect(Collectors.toMap(data -> (String) data[0], data -> (MessageAttributeValue) data[1]));

	}

	private static String getOrderConfirmationCode(String orderCode) {
		try {
			return Base64.getEncoder().encodeToString(orderCode.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to convert the Order code to Base64");
		}
		return null;
	}

	private static String formatBody(List<Product> items) {
		StringBuilder sb = new StringBuilder();
		sb.append("Following products are reserved:").append(LINE_BREAK).append(LINE_BREAK);
		for (Product product : items) {
			sb.append(product.getName()).append(SPACE).append(product.getPrice()).append(LINE_BREAK);
		}
		return sb.toString();
	}

}
