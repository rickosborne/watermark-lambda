package org.rickosborne.watermark.lambda;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

import static org.rickosborne.watermark.lambda.ConfigUtil.getBoolean;
import static org.rickosborne.watermark.lambda.ConfigUtil.getFloat;
import static org.rickosborne.watermark.lambda.ConfigUtil.getInteger;
import static org.rickosborne.watermark.lambda.ConfigUtil.getString;

@Value
@Builder(toBuilder = true)
@NonFinal
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WatermarkConfig {
	String inboxBucketName;
	String inboxBucketPath;
	String outboxBucketName;
	String outboxBucketPath;
	Boolean publicDestination;
	Boolean removeSourceOnSuccess;
	Integer renameKeyLength;
	Boolean skipIfPresent;
	Integer watermarkBottom;
	MeasureUnit watermarkBottomUnit;
	String watermarkBucketName;
	String watermarkBucketPath;
	Integer watermarkHeight;
	MeasureUnit watermarkHeightUnit;
	Integer watermarkLeft;
	MeasureUnit watermarkLeftUnit;
	Float watermarkOpacity;
	Integer watermarkRight;
	MeasureUnit watermarkRightUnit;
	Integer watermarkTop;
	MeasureUnit watermarkTopUnit;
	Integer watermarkWidth;
	MeasureUnit watermarkWidthUnit;

	public static WatermarkConfig build() {
		return new WatermarkConfig(
			getString("s3.inbox.bucket").orElse(null),  // use what's in the event by default
			getString("s3.inbox.path").orElse(null),
			getString("s3.outbox.bucket").orElse(null),
			getString("s3.outbox.path").orElse(null),
			getBoolean("s3.outbox.public").orElse(null),
			getBoolean("s3.inbox.removeOnSuccess").orElse(null),
			getInteger("rename.key.length").orElse(null),
			getBoolean("s3.outbox.skipIfPresent").orElse(null),
			getInteger("watermark.bottom.value").orElse(null),
			getString("watermark.bottom.units").map(MeasureUnit::fromString).orElse(null),
			getString("s3.watermark.bucket").orElse(null),
			getString("s3.watermark.path").orElse(null),
			getInteger("watermark.height.value").orElse(null),
			getString("watermark.height.units").map(MeasureUnit::fromString).orElse(null),
			getInteger("watermark.left.value").orElse(null),
			getString("watermark.left.units").map(MeasureUnit::fromString).orElse(null),
			getFloat("watermark.opacity").orElse(null),
			getInteger("watermark.right.value").orElse(null),
			getString("watermark.right.units").map(MeasureUnit::fromString).orElse(null),
			getInteger("watermark.top.value").orElse(null),
			getString("watermark.top.units").map(MeasureUnit::fromString).orElse(null),
			getInteger("watermark.width.value").orElse(null),
			getString("watermark.width.units").map(MeasureUnit::fromString).orElse(null)
		);
	}
}
