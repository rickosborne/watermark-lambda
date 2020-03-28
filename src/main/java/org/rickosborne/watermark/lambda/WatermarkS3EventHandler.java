package org.rickosborne.watermark.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Event;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;

@SuppressWarnings("unused")  // Used by AWS
public class WatermarkS3EventHandler extends AWatermarkRequestHandler<com.amazonaws.services.lambda.runtime.events.S3Event> {
	private static final Set<S3Event> S3EVENT_WHITELIST = Stream.of(
		S3Event.ObjectCreated,
		S3Event.ObjectCreatedByPost,
		S3Event.ObjectCreatedByPut,
		S3Event.ObjectCreatedByCopy,
		S3Event.ObjectCreatedByCompleteMultipartUpload
	).collect(Collectors.toUnmodifiableSet());

	protected WatermarkPostRequest configFromS3Event(
		@NonNull final S3EventNotification.S3EventNotificationRecord notification,
		@NonNull final WatermarkLogger logger,
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
	public WatermarkPostResponse handleRequest(
		@NonNull final com.amazonaws.services.lambda.runtime.events.S3Event input,
		@NonNull final Context context
	) {
		final SlackLogger slackLogger = SlackLogger.fromConfig(getDefaultConfig(), context.getLogger());
		final WatermarkLogger logger = new WatermarkLogger(
			context.getLogger(),
			slackLogger
		);
		final String requestId = context.getAwsRequestId();
		for (final S3EventNotification.S3EventNotificationRecord notification : input.getRecords()) {
			final WatermarkPostRequest requestConfig = configFromS3Event(notification, logger, requestId);
			logger.debug(requestConfig.toString());
			//noinspection ConstantConditions
			if (requestConfig != null) {
				final WatermarkPostResponse response = handle(new WatermarkRequest(
					getDefaultConfig(),
					logger,
					requestConfig,
					requestId
				));
				if (response != null) {
					logResponse(logger, response);
				}
			}
		}
		return null;
	}
}
