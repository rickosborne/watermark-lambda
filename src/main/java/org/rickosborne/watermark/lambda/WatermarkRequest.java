package org.rickosborne.watermark.lambda;

import lombok.NonNull;
import lombok.Value;

@Value
public class WatermarkRequest {
	@NonNull
	WatermarkConfig defaultConfig;
	@NonNull
	WatermarkLogger logger;
	@NonNull
	WatermarkPostRequest requestConfig;
	@NonNull
	String requestId;
}
