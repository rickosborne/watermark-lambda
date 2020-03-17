package org.rickosborne.watermark.lambda;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;

@Getter
public class TestableLogger implements LambdaLogger {
	private final List<byte[]> bytes = new LinkedList<>();
	private final List<String> messages = new LinkedList<>();

	@Override
	public void log(final String message) {
		System.out.println(message);
		messages.add(message);
	}

	@Override
	public void log(final byte[] message) {
		System.out.println(new String(message));
		bytes.add(message);
	}
}
