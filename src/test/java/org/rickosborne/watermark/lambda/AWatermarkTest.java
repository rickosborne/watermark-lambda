package org.rickosborne.watermark.lambda;

import java.io.InputStream;

public abstract class AWatermarkTest {
	public static WatermarkConfig buildWatermarkConfig() {
		return WatermarkConfig.builder()
			.build();
	}

	public static InputStream getWatermarkStream() {
		return AWatermarkTest.class.getResourceAsStream("/watermark.png");
	}

	public static InputStream getExampleJpegStream() {
		return AWatermarkTest.class.getResourceAsStream("/example.jpg");
	}

	public static InputStream getExamplePngStream() {
		return AWatermarkTest.class.getResourceAsStream("/example.png");
	}
}
