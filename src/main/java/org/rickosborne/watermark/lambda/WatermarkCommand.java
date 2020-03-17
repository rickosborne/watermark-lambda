package org.rickosborne.watermark.lambda;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class WatermarkCommand {
	public static void main(String[] args) throws JsonProcessingException {
		final String sourceKey = args[0];
		final WatermarkPostRequest post = WatermarkPostRequest.builder()
			.sourceKey(sourceKey)
			.build();
		final WatermarkPostResponse response = new WatermarkPostHandler()
			.handleRequest(post, new CommandContext());
		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));
		if (!response.isSuccess()) {
			System.exit(-1);
		}
	}

	@Value
	private static class CommandContext implements Context {
		String awsRequestId = "commandRequestId";
		ClientContext clientContext = null;
		String functionName = "commandFunctionName";
		String functionVersion = "commandFunctionVersion";
		CognitoIdentity identity = null;
		String invokedFunctionArn = "commandInvokedFunctionArn";
		String logGroupName = "commandLogGroupName";
		String logStreamName = "commandLogStreamName";
		CommandLogger logger = new CommandLogger();
		int memoryLimitInMB = 0;
		int remainingTimeInMillis = 0;
	}

	private static class CommandLogger implements LambdaLogger {
		@Override
		public void log(final String message) {
			System.out.println(message);
		}

		@Override
		public void log(final byte[] message) {
			System.out.println(new String(message));
		}
	}
}
