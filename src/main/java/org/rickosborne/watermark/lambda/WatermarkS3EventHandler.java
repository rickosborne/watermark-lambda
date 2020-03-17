package org.rickosborne.watermark.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Event;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;

@SuppressWarnings("unused")  // Used by AWS
public class WatermarkS3EventHandler extends AWatermarkRequestHandler<com.amazonaws.services.lambda.runtime.events.S3Event, Void> {
	private static final Set<S3Event> S3EVENT_WHITELIST = Stream.of(
		S3Event.ObjectCreated,
		S3Event.ObjectCreatedByPost,
		S3Event.ObjectCreatedByPut,
		S3Event.ObjectCreatedByCopy,
		S3Event.ObjectCreatedByCompleteMultipartUpload
	).collect(Collectors.toUnmodifiableSet());

	protected WatermarkPostRequest configFromS3Event(
		@NonNull final S3EventNotification.S3EventNotificationRecord notification,
		@NonNull final Lambda2Logger logger,
		@NonNull final String requestId
	) {
		final S3EventNotification.S3Entity s3Entity = notification.getS3();
		if (s3Entity == null) {
			logger.error("Missing S3 key in event: " + requestId);
			return null;
		}
		if (!S3EVENT_WHITELIST.contains(notification.getEventNameAsEnum())) {
			logger.debug("Ignoring event type '" + notification.getEventName() + "': " + requestId);
			return null;
		}
		final S3EventNotification.S3BucketEntity bucket = s3Entity.getBucket();
		final S3EventNotification.S3ObjectEntity object = s3Entity.getObject();
		if (bucket == null || bucket.getName() == null || object == null || object.getKey() == null) {
			logger.error("Missing S3 bucket/object in event: " + requestId);
			return null;
		}
		return postBuilderFromDefaultConfig()
			.sourceKey(object.getKey())
			.sourceBucket(bucket.getName())
			.build();
	}

	@Override
	public Void handleRequest(@NonNull final com.amazonaws.services.lambda.runtime.events.S3Event input, @NonNull final Context context) {
		final Lambda2Logger logger = new Lambda2Logger(context.getLogger());
		final String requestId = context.getAwsRequestId();
		for (final S3EventNotification.S3EventNotificationRecord notification : input.getRecords()) {
			final WatermarkPostRequest requestConfig = configFromS3Event(notification, logger, requestId);
			logger.debug(requestConfig.toString());
			if (requestConfig != null) {
				return handle(
					new WatermarkRequest(
						getDefaultConfig(),
						logger,
						requestConfig,
						requestId
					)
				);
			}
		}
		return null;
	}

	@Override
	protected Void responseForFailedDestinationKey(final @NonNull WatermarkRequest request) {
		return null;
	}

	@Override
	protected Void responseForFailedWrite(final WatermarkRequest request, final IOException e) {
		return null;
	}

	@Override
	protected Void responseForImpossibleDestination(final @NonNull WatermarkRequest request) {
		return null;
	}

	@Override
	protected Void responseForMinusculeWatermark(final @NonNull WatermarkRequest request) {
		return null;
	}

	@Override
	protected Void responseForMissingBucket(final WatermarkRequest request) {
		return null;
	}

	@Override
	protected Void responseForMissingSourceImage(final @NonNull WatermarkRequest request) {
		return null;
	}

	@Override
	protected Void responseForMissingWatermarkImage(final @NonNull WatermarkRequest request) {
		return null;
	}

	@Override
	protected Void responseForSkipped(final @NonNull WatermarkRequest request, final String destinationKey) {
		return null;
	}

	@Override
	protected Void responseForSuccess(final WatermarkRequest request, final BufferedImage sourceImage, final String sourceImageFormat, final String destinationKey) {
		return null;
	}

	@Override
	protected Void responseForUnknownImageFormat(final BufferedImage sourceImage, final String sourceImageFormat) {
		return null;
	}
}
