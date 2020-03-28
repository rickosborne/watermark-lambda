package org.rickosborne.watermark.lambda;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WatermarkLogger {
	private final LambdaLogger lambdaLogger;
	private final SlackLogger slackLogger;

	public void debug(final String message) {
		logMessage("[DEBUG] " + message);
		// no Slack messages here
	}

	public void error(final String message) {
		logMessage("[ERROR] " + message);
		if (slackLogger != null) {
			slackLogger.send("ERROR: " + message, false);
		}
	}

	public void error(final String message, final Throwable error) {
		if (error != null && error.getMessage() != null) {
			error(message + ": " + error.getMessage());
		} else {
			error(message);
		}
	}

	public void info(final String message) {
		logMessage("[INFO] " + message);
		if (slackLogger != null) {
			slackLogger.send(message, false);
		}
	}

	private void logMessage(final String message) {
		lambdaLogger.log(message);
	}
}
