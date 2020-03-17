package org.rickosborne.watermark.lambda;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

public class TestableImageProcessor extends WatermarkImageProcessor {
	@Getter
	private final List<WatermarkCall> watermarkCalls = new LinkedList<>();

	@Override
	public void watermark(
		final @NonNull BufferedImage sourceImage,
		final @NonNull BufferedImage watermarkImage,
		final @NonNull Rectangle destination,
		final @NonNull WatermarkPostRequest requestConfig
	) {
		watermarkCalls.add(new WatermarkCall(destination, requestConfig, sourceImage, watermarkImage));
		super.watermark(sourceImage, watermarkImage, destination, requestConfig);
	}

	@Value
	public static class WatermarkCall {
		@NonNull Rectangle destination;
		@NonNull WatermarkPostRequest requestConfig;
		@NonNull BufferedImage sourceImage;
		@NonNull BufferedImage watermarkImage;
	}
}
