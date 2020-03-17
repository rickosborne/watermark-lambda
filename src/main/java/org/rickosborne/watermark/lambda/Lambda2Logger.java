package org.rickosborne.watermark.lambda;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Lambda2Logger {
	private final LambdaLogger logger;

	public void debug(final String message) {
		logger.log("[DEBUG] " + message);
	}

	public void error(final String message) {
		logger.log("[ERROR] " + message);
	}

	public void error(final String message, final Throwable error) {
		if (error != null && error.getMessage() != null) {
			error(message + ": " + error.getMessage());
		} else {
			error(message);
		}
	}

	public void info(final String message) {
		logger.log("[INFO] " + message);
	}
}
