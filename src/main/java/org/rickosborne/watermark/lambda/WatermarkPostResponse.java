package org.rickosborne.watermark.lambda;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WatermarkPostResponse {
	String destinationKey;
	String errorMessage;
	String failureReason;
	Boolean skipped;
	boolean success;

	public static WatermarkPostResponse fail(@NonNull final String failureReason) {
		return new WatermarkPostResponse(null, null, failureReason, null, false);
	}

	public static WatermarkPostResponse fail(@NonNull final String failureReason, final Throwable error) {
		return new WatermarkPostResponse(null, error != null ? error.getMessage() : null, failureReason, null, false);
	}

	public static WatermarkPostResponse ok(final String destinationKey) {
		return new WatermarkPostResponse(destinationKey, null, null, null, true);
	}

	public static WatermarkPostResponse skipped(final String destinationKey) {
		return new WatermarkPostResponse(destinationKey, null, null, true, true);
	}
}
