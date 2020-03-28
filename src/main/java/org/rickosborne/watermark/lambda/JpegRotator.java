package org.rickosborne.watermark.lambda;

import java.awt.image.BufferedImage;
import lombok.NonNull;
import lombok.Value;

public class JpegRotator {
	public RotationResult rotateIfNecessary(
		@NonNull final BufferedImage image
	) {
		return new RotationResult(false, image, image);
	}

	@Value
	public static class RotationResult {
		boolean different;
		BufferedImage originalImage;
		BufferedImage updatedImage;
	}
}
