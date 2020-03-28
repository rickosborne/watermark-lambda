package org.rickosborne.watermark.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.NonNull;

public class WatermarkPostHandler extends AWatermarkRequestHandler<WatermarkPostRequest> {
	/**
	 * Mostly for testing.
	 */
	public WatermarkPostHandler(
		@NonNull final WatermarkConfig defaultConfig,
		@NonNull final WatermarkImageProcessor imageProcessor,
		@NonNull final WatermarkStore storage
	) {
		super(defaultConfig, storage, imageProcessor);
	}

	/**
	 * This is the one AWS will use.
	 */
	public WatermarkPostHandler() {
		super();
	}

	@Override
	public WatermarkPostResponse handleRequest(
		@NonNull final WatermarkPostRequest post,
		@NonNull final Context context
	) {
		final WatermarkLogger logger = new WatermarkLogger(
			context.getLogger(),
			SlackLogger.fromConfig(getDefaultConfig(), context.getLogger())
		);
		try {
			final WatermarkPostResponse response = handle(requestFromPost(post, logger, context.getAwsRequestId()));
			if (response == null) {
				return WatermarkPostResponse.fail("Null response from handler");
			} else if (response.getDestinationKey() != null) {
				logResponse(logger, response);
			}
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Caught exception", e);
			return WatermarkPostResponse.fail(e.getMessage());
		}
	}

	protected WatermarkRequest requestFromPost(
		@NonNull final WatermarkPostRequest post,
		@NonNull final WatermarkLogger logger,
		@NonNull final String requestId
	) {
		return new WatermarkRequest(
			getDefaultConfig(),
			logger,
			Field.overlayAll(postBuilderFromDefaultConfig(), post).build(),
			requestId
		);
	}

	@SuppressWarnings("unused")
	enum Field {
		DEST_BUCKET(WatermarkPostRequest::getDestinationBucket, WatermarkPostRequest.WatermarkPostRequestBuilder::destinationBucket),
		DEST_PATH(WatermarkPostRequest::getDestinationKey, WatermarkPostRequest.WatermarkPostRequestBuilder::destinationKey),
		DEST_URL(WatermarkPostRequest::getDestinationUrl, WatermarkPostRequest.WatermarkPostRequestBuilder::destinationUrl),
		PUB_DEST(WatermarkPostRequest::getPublicDestination, WatermarkPostRequest.WatermarkPostRequestBuilder::publicDestination),
		REMOVE_ON_SUCCESS(WatermarkPostRequest::getRemoveSourceOnSuccess, WatermarkPostRequest.WatermarkPostRequestBuilder::removeSourceOnSuccess),
		RENAME_KEY(WatermarkPostRequest::getRenameKeyLength, WatermarkPostRequest.WatermarkPostRequestBuilder::renameKeyLength),
		SKIP_IF_PRESENT(WatermarkPostRequest::getSkipIfPresent, WatermarkPostRequest.WatermarkPostRequestBuilder::skipIfPresent),
		SLACK_APPID(WatermarkPostRequest::getSlackAppId, WatermarkPostRequest.WatermarkPostRequestBuilder::slackAppId),
		SLACK_BOTOAUTH(WatermarkPostRequest::getSlackBotOauth, WatermarkPostRequest.WatermarkPostRequestBuilder::slackBotOauth),
		SLACK_CLIENTID(WatermarkPostRequest::getSlackClientId, WatermarkPostRequest.WatermarkPostRequestBuilder::slackClientId),
		SLACK_OAUTH(WatermarkPostRequest::getSlackOauth, WatermarkPostRequest.WatermarkPostRequestBuilder::slackOauth),
		SLACK_SIGNSEC(WatermarkPostRequest::getSlackSigningSecret, WatermarkPostRequest.WatermarkPostRequestBuilder::slackSigningSecret),
		SLACK_TOKEN(WatermarkPostRequest::getSlackToken, WatermarkPostRequest.WatermarkPostRequestBuilder::slackToken),
		SLACK_VERTOKEN(WatermarkPostRequest::getSlackVerificationToken, WatermarkPostRequest.WatermarkPostRequestBuilder::slackVerificationToken),
		SOURCE_BUCKET(WatermarkPostRequest::getSourceBucket, WatermarkPostRequest.WatermarkPostRequestBuilder::sourceBucket),
		SOURCE_KEY(WatermarkPostRequest::getSourceKey, WatermarkPostRequest.WatermarkPostRequestBuilder::sourceKey),
		WM_BOTTOM(WatermarkPostRequest::getWatermarkBottom, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkBottom),
		WM_BOTTOM_UNIT(WatermarkPostRequest::getWatermarkBottomUnit, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkBottomUnit),
		WM_BUCKET(WatermarkPostRequest::getWatermarkBucketName, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkBucketName),
		WM_PATH(WatermarkPostRequest::getWatermarkBucketPath, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkBucketPath),
		WM_HEIGHT(WatermarkPostRequest::getWatermarkHeight, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkHeight),
		WM_HEIGHT_UNIT(WatermarkPostRequest::getWatermarkHeightUnit, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkHeightUnit),
		WM_LEFT(WatermarkPostRequest::getWatermarkLeft, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkLeft),
		WM_LEFT_UNIT(WatermarkPostRequest::getWatermarkLeftUnit, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkLeftUnit),
		WM_OPACITY(WatermarkPostRequest::getWatermarkOpacity, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkOpacity),
		WM_RIGHT(WatermarkPostRequest::getWatermarkRight, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkRight),
		WM_RIGHT_UNIT(WatermarkPostRequest::getWatermarkRightUnit, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkRightUnit),
		WM_TOP(WatermarkPostRequest::getWatermarkTop, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkTop),
		WM_TOP_UNIT(WatermarkPostRequest::getWatermarkTopUnit, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkTopUnit),
		WM_WIDTH(WatermarkPostRequest::getWatermarkWidth, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkWidth),
		WM_WIDTH_UNIT(WatermarkPostRequest::getWatermarkWidthUnit, WatermarkPostRequest.WatermarkPostRequestBuilder::watermarkWidthUnit),
		;
		@NonNull
		final BiFunction<WatermarkPostRequest.WatermarkPostRequestBuilder, Object, WatermarkPostRequest.WatermarkPostRequestBuilder> configSetter;
		@NonNull
		final Function<WatermarkPostRequest, Object> requestGetter;

		@SuppressWarnings("unchecked")
		<T> Field(
			@NonNull final Function<WatermarkPostRequest, T> requestGetter,
			@NonNull final BiFunction<WatermarkPostRequest.WatermarkPostRequestBuilder, T, WatermarkPostRequest.WatermarkPostRequestBuilder> configSetter
		) {
			this.requestGetter = (Function<WatermarkPostRequest, Object>) requestGetter;
			this.configSetter = (BiFunction<WatermarkPostRequest.WatermarkPostRequestBuilder, Object, WatermarkPostRequest.WatermarkPostRequestBuilder>) configSetter;
		}

		public static WatermarkPostRequest.WatermarkPostRequestBuilder overlayAll(
			@NonNull WatermarkPostRequest.WatermarkPostRequestBuilder configBuilder,
			@NonNull final WatermarkPostRequest post
		) {
			for (final Field field : values()) {
				configBuilder = field.overlay(configBuilder, post);
			}
			return configBuilder;
		}

		protected WatermarkPostRequest.WatermarkPostRequestBuilder overlay(
			@NonNull final WatermarkPostRequest.WatermarkPostRequestBuilder builder,
			@NonNull final WatermarkPostRequest post
		) {
			final Object requestValue = requestGetter.apply(post);
			return requestValue == null ? builder : configSetter.apply(builder, requestValue);
		}
	}
}
