package org.rickosborne.watermark.lambda;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import lombok.NonNull;

public class WatermarkImageProcessor {
	public void watermark(
		@NonNull final BufferedImage sourceImage,
		@NonNull final BufferedImage watermarkImage,
		@NonNull final Rectangle destination,
		@NonNull final WatermarkPostRequest requestConfig
	) {
		final Graphics2D graphics = (Graphics2D) sourceImage.getGraphics();
		final AlphaComposite alphaComposite = AlphaComposite
			.getInstance(AlphaComposite.SRC_OVER, requestConfig.getWatermarkOpacityOrDefault());
		graphics.setComposite(alphaComposite);
		graphics.drawImage(watermarkImage, destination.x, destination.y, destination.width, destination.height, null);
	}
}
