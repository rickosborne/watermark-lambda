package org.rickosborne.watermark.lambda;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Getter(value = AccessLevel.PROTECTED)
@RequiredArgsConstructor
public abstract class AWatermarkRequestHandler<RequestT, ResponseT> implements RequestHandler<RequestT, ResponseT> {
	private final WatermarkConfig defaultConfig;
	private final WatermarkImageProcessor imageProcessor;
	private final WatermarkStore fileStore;

	protected AWatermarkRequestHandler() {
		this(
			WatermarkConfig.build(),
			new WatermarkImageProcessor(),
			WatermarkStoreFactory.buildStore()
		);
	}

	private Pair<BufferedImage, String> getBufferedImageFromStore(
		@NonNull final String bucketName,
		@NonNull final String bucketPath,
		@NonNull final WatermarkRequest request
	) {
		request.getLogger().debug(String.format("getBufferedImageFromStore(%s, %s)", bucketName, bucketPath));
		try {
			final InputStream imageIn = fileStore.read(bucketName, bucketPath, request.getLogger());
			if (imageIn == null) {
				return null;
			}
			final ImageInputStream imageInputStream = ImageIO.createImageInputStream(imageIn);
			final ImageReader imageReader = ImageIO.getImageReaders(imageInputStream).next();
			final BufferedImage image = ImageIO.read(imageInputStream);
			final String formatName = imageReader == null ? null : imageReader.getFormatName();
			return new Pair<>(image, formatName);
		} catch (IOException e) {
			request.getLogger().error(String.format("Could not open image input stream: %s:%s", bucketName, bucketPath), e);
		}
		return null;
	}

	private String getDestinationKey(
		@NonNull final WatermarkRequest request,
		@NonNull final BufferedImage sourceImage,
		@NonNull final String sourceImageFormat
	) {
		request.getLogger().debug(String.format("getDestinationKey(%s)", sourceImageFormat));
		final WatermarkPostRequest requestConfig = request.getRequestConfig();
		final Integer renameKeyLength = requestConfig.getRenameKeyLength();
		final String destinationKey = requestConfig.getDestinationKey();
		if (renameKeyLength != null) {  // keep the same name
			try {
				final MessageDigest digest = MessageDigest.getInstance("SHA-256");
				try (final DigestOutputStream bytesOut = new DigestOutputStream(OutputStream.nullOutputStream(), digest)) {
					if (ImageIO.write(sourceImage, "bmp", bytesOut)) {
						final byte[] hashBytes = digest.digest();
						final String base64Hash = Base64.getUrlEncoder().encodeToString(hashBytes);
						final String key = (renameKeyLength >= base64Hash.length()
							? base64Hash
							: base64Hash.substring(0, renameKeyLength))
							.replace("=", "");
						return destinationKey == null ? key : (destinationKey + key + "." + sourceImageFormat.toLowerCase());
					}
				} catch (IOException e) {
					request.getLogger().error("Failed to get image digest", e);
				}
			} catch (NoSuchAlgorithmException e) {
				request.getLogger().error("Could not set up message digest", e);
			}
		}
		final String sourceKey = requestConfig.getSourceKey();
		final Matcher matcher = Pattern.compile("(^|.*?/)([^/]+)$").matcher(sourceKey);
		if (!matcher.matches()) {
			request.getLogger().error("Could not derive filename part of S3 path: " + sourceKey);
			return null;
		}
		final String fileName = matcher.group(2);
		return destinationKey == null ? fileName : (destinationKey + fileName);
	}

	private Pair<BufferedImage, String> getSourceImageStream(@NonNull final WatermarkRequest request) {
		request.getLogger().debug("getSourceImageStream()");
		final WatermarkPostRequest config = request.getRequestConfig();
		final String sourceBucket = config.getSourceBucket();
		final String sourceKey = config.getSourceKey();
		if (sourceBucket == null || sourceKey == null || sourceBucket.isBlank() || sourceKey.isBlank()) {
			request.getLogger().error("No source bucket/path provided");
			return null;
		}
		return getBufferedImageFromStore(sourceBucket, sourceKey, request);
	}

	private Pair<BufferedImage, String> getWatermarkImageStream(
		@NonNull final WatermarkRequest request
	) {
		final WatermarkPostRequest config = request.getRequestConfig();
		final String watermarkBucketName = config.getWatermarkBucketName();
		final String watermarkBucketPath = config.getWatermarkBucketPath();
		if (watermarkBucketName == null || watermarkBucketPath == null || watermarkBucketName.isBlank() || watermarkBucketPath.isBlank()) {
			request.getLogger().error("No watermark bucket/path provided");
			return null;
		}
		return getBufferedImageFromStore(watermarkBucketName, watermarkBucketPath, request);
	}

	protected ResponseT handle(
		@NonNull final WatermarkRequest request
	) {
		request.getLogger().debug("handle()");
		final Pair<BufferedImage, String> sourceImagePair = getSourceImageStream(request);
		if (sourceImagePair == null) {
			request.getLogger().error("Could not resolve source image");
			return responseForMissingSourceImage(request);
		}
		final BufferedImage sourceImage = sourceImagePair.getLeft();
		final String sourceImageFormat = sourceImagePair.getRight();
		final String destinationKey = getDestinationKey(request, sourceImage, sourceImageFormat);
		if (destinationKey == null) {
			request.getLogger().error("Could not resolve destination key");
			return responseForFailedDestinationKey(request);
		}
		final WatermarkPostRequest requestConfig = request.getRequestConfig();
		final String destinationBucket = requestConfig.getDestinationBucket();
		if (destinationBucket == null) {
			request.getLogger().error("Missing destination bucket");
			return responseForMissingBucket(request);
		}
		if (requestConfig.isSkipIfPresentOrDefault() && fileStore.exists(destinationBucket, destinationKey, request.getLogger())) {
			request.getLogger().info("Skipping because destination exists: " + destinationBucket + ":" + destinationKey);
			return responseForSkipped(request, destinationKey);
		}
		final Pair<BufferedImage, String> watermarkImagePair = getWatermarkImageStream(request);
		if (watermarkImagePair == null) {
			request.getLogger().error("Could not resolve watermark image");
			return responseForMissingWatermarkImage(request);
		}
		final BufferedImage watermarkImage = watermarkImagePair.getLeft();
		final Rectangle destination = new WatermarkBoundsCalculator().getBounds(request, sourceImage, watermarkImage);
		if (destination == null) {
			request.getLogger().error("Could not calculate a destination");
			return responseForImpossibleDestination(request);
		} else if (destination.width == 0 || destination.height == 0) {
			request.getLogger().error("Watermark was too small");
			return responseForMinusculeWatermark(request);
		}
		imageProcessor.watermark(sourceImage, watermarkImage, destination, requestConfig);
		return writeImageToStore(request, sourceImage, destinationKey, sourceImageFormat);
	}

	protected WatermarkPostRequest.WatermarkPostRequestBuilder postBuilderFromDefaultConfig() {
		return WatermarkPostRequest.builder()
			.destinationBucket(defaultConfig.getOutboxBucketName())
			.destinationKey(defaultConfig.getOutboxBucketPath())
			.publicDestination(defaultConfig.getPublicDestination())
			.removeSourceOnSuccess(defaultConfig.getRemoveSourceOnSuccess())
			.renameKeyLength(defaultConfig.getRenameKeyLength())
			.skipIfPresent(defaultConfig.getSkipIfPresent())
			.sourceBucket(defaultConfig.getInboxBucketName())
			.sourceKey(defaultConfig.getInboxBucketPath())
			.watermarkBottom(defaultConfig.getWatermarkBottom())
			.watermarkBottomUnit(defaultConfig.getWatermarkBottomUnit())
			.watermarkBucketName(defaultConfig.getWatermarkBucketName())
			.watermarkBucketPath(defaultConfig.getWatermarkBucketPath())
			.watermarkHeight(defaultConfig.getWatermarkHeight())
			.watermarkHeightUnit(defaultConfig.getWatermarkHeightUnit())
			.watermarkLeft(defaultConfig.getWatermarkLeft())
			.watermarkLeftUnit(defaultConfig.getWatermarkLeftUnit())
			.watermarkOpacity(defaultConfig.getWatermarkOpacity())
			.watermarkRight(defaultConfig.getWatermarkRight())
			.watermarkRightUnit(defaultConfig.getWatermarkRightUnit())
			.watermarkTop(defaultConfig.getWatermarkTop())
			.watermarkTopUnit(defaultConfig.getWatermarkTopUnit())
			.watermarkWidth(defaultConfig.getWatermarkWidth())
			.watermarkWidthUnit(defaultConfig.getWatermarkWidthUnit());
	}

	protected abstract ResponseT responseForFailedDestinationKey(@NonNull final WatermarkRequest request);

	protected abstract ResponseT responseForFailedWrite(final WatermarkRequest request, final IOException e);

	protected abstract ResponseT responseForImpossibleDestination(@NonNull final WatermarkRequest request);

	protected abstract ResponseT responseForMinusculeWatermark(@NonNull final WatermarkRequest request);

	protected abstract ResponseT responseForMissingBucket(final WatermarkRequest request);

	protected abstract ResponseT responseForMissingSourceImage(@NonNull final WatermarkRequest request);

	protected abstract ResponseT responseForMissingWatermarkImage(@NonNull final WatermarkRequest request);

	protected abstract ResponseT responseForSkipped(@NonNull final WatermarkRequest request, final String resultKey);

	protected abstract ResponseT responseForSuccess(final WatermarkRequest request, final BufferedImage sourceImage, final String sourceImageFormat, final String destinationKey);

	protected abstract ResponseT responseForUnknownImageFormat(final BufferedImage sourceImage, final String sourceImageFormat);

	private ResponseT writeImageToStore(
		@NonNull final WatermarkRequest request,
		@NonNull final BufferedImage sourceImage,
		@NonNull final String destinationKey,
		@NonNull final String sourceImageFormat
	) {
		request.getLogger().debug(String.format("writeImageToStore(%s, %s)", destinationKey, sourceImageFormat));
		@NonNull final WatermarkPostRequest requestConfig = request.getRequestConfig();
		final String destinationBucket = requestConfig.getDestinationBucket();
		if (destinationBucket == null) {
			request.getLogger().error("Could not resolve destination bucket");
			return responseForMissingBucket(request);
		}
		try (final ByteArrayOutputStream outBytes = new ByteArrayOutputStream()) {
			if (!ImageIO.write(sourceImage, sourceImageFormat, outBytes)) {
				request.getLogger().error("Unknown image format: " + sourceImageFormat);
				return responseForUnknownImageFormat(sourceImage, sourceImageFormat);
			}
			final long contentLength = outBytes.size();
			fileStore.write(
				destinationBucket,
				destinationKey,
				outBytes,
				"image/" + sourceImageFormat.toLowerCase(),
				contentLength,
				requestConfig.isPublicDestinationOrDefault(),
				request.getLogger()
			);
			if (requestConfig.isRemoveSourceOnSuccessOrDefault()) {
				fileStore.delete(requestConfig.getSourceBucket(), requestConfig.getSourceKey(), request.getLogger());
			}
			return responseForSuccess(request, sourceImage, sourceImageFormat, destinationKey);
		} catch (IOException e) {
			request.getLogger().error("Failed to write image to S3", e);
			return responseForFailedWrite(request, e);
		}
	}

	@Value
	protected static class Pair<A, B> {
		A left;
		B right;
	}
}
