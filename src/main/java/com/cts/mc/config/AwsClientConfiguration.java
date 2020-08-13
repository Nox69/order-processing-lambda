package com.cts.mc.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

/**
 * @author bharatkumar
 *
 */
public class AwsClientConfiguration {

	private AwsClientConfiguration() {
		// Utility classes should not have public constructors (squid:S1118)
	}
	
	public static String s = "AKIAJTEHKQHQCH4P2XIQ";
	public static String k = "D6IvUOh6gkYp435G/DYXYaykpLxKih1ntB2xHNZD";

	public static AWSCredentials credentials() {
		return new BasicAWSCredentials(s,k);
	}

	public static AmazonSQS sqsClient() {
		return AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials()))
				.withRegion(Regions.US_EAST_2).build();
	}

}
