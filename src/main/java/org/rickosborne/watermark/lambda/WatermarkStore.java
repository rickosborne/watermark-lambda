package org.rickosborne.watermark.lambda;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import lombok.NonNull;

public interface WatermarkStore {
	void delete(
		@NonNull String bucketName,
		@NonNull String key,
		@NonNull WatermarkLogger logger
	);

	boolean exists(
		@NonNull String bucketName,
		@NonNull String key,
		@NonNull WatermarkLogger logger
	);

	InputStream read(
		@NonNull String bucketName,
		@NonNull String key,
		@NonNull WatermarkLogger logger
	);

	void write(
		@NonNull String bucketName,
		@NonNull String key,
		@NonNull ByteArrayOutputStream outBytes,
		@NonNull String contentType,
		Long contentLength,
		Boolean makePublic,
		@NonNull WatermarkLogger logger
	);
}
