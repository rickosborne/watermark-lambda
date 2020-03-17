package org.rickosborne.watermark.lambda;

import lombok.NonNull;
import lombok.Value;

@Value
public class WatermarkRequest {
	@NonNull
	WatermarkConfig defaultConfig;
	@NonNull
	Lambda2Logger logger;
	@NonNull
	WatermarkPostRequest requestConfig;
	@NonNull
	String requestId;
}
