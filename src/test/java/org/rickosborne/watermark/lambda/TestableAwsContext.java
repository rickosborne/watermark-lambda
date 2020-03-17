package org.rickosborne.watermark.lambda;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import lombok.Value;

import static org.rickosborne.watermark.lambda.TestUtil.randomString;

@Value
public class TestableAwsContext implements Context {
	String awsRequestId = "testRequestId-" + randomString();
	String functionName = "testFunctionName-" + randomString();
	String functionVersion = "testFunctionVersion-" + randomString();
	String invokedFunctionArn = "testInvokedFunctionArn-" + randomString();
	String logGroupName = "testLogGroupName-" + randomString();
	String logStreamName = "testLogStreamName-" + randomString();
	TestableLogger logger = new TestableLogger();
	int memoryLimitInMB = 0;
	int remainingTimeInMillis = 0;

	@Override
	public ClientContext getClientContext() {
		throw new RuntimeException("Unexpected call to getClientContext()");
	}

	@Override
	public CognitoIdentity getIdentity() {
		throw new RuntimeException("Unexpected call to getIdentity()");
	}
}
