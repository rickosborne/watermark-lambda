package org.rickosborne.watermark.lambda;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder(toBuilder = true)
@ToString
@AllArgsConstructor
public class WatermarkPostRequest {
	public static final float OPACITY_DEFAULT = 1.0f;
	public static final boolean PUBLIC_DESTINATION_DEFAULT = false;
	public static final boolean REMOVE_SOURCE_ON_SUCCESS_DEFAULT = false;
	public static final boolean SKIP_IF_PRESENT_DEFAULT = true;
	public static final MeasureUnit UNIT_DEFAULT = MeasureUnit.PIXELS;

	private String destinationBucket;
	private String destinationKey;
	private Boolean publicDestination;
	private Boolean removeSourceOnSuccess;
	private Integer renameKeyLength;
	private Boolean skipIfPresent;
	private String sourceBucket;
	private String sourceKey;
	private Integer watermarkBottom;
	private MeasureUnit watermarkBottomUnit;
	private String watermarkBucketName;
	private String watermarkBucketPath;
	private Integer watermarkHeight;
	private MeasureUnit watermarkHeightUnit;
	private Integer watermarkLeft;
	private MeasureUnit watermarkLeftUnit;
	private Float watermarkOpacity;
	private Integer watermarkRight;
	private MeasureUnit watermarkRightUnit;
	private Integer watermarkTop;
	private MeasureUnit watermarkTopUnit;
	private Integer watermarkWidth;
	private MeasureUnit watermarkWidthUnit;

	public WatermarkPostRequest() {
	}

	public MeasureUnit getWatermarkBottomUnitOrDefault() {
		return watermarkBottomUnit == null ? UNIT_DEFAULT : watermarkBottomUnit;
	}

	public MeasureUnit getWatermarkHeightUnitOrDefault() {
		return watermarkHeightUnit == null ? UNIT_DEFAULT : watermarkHeightUnit;
	}

	public MeasureUnit getWatermarkLeftUnitOrDefault() {
		return watermarkLeftUnit == null ? UNIT_DEFAULT : watermarkLeftUnit;
	}

	public float getWatermarkOpacityOrDefault() {
		return watermarkOpacity == null
			|| watermarkOpacity < 0
			|| watermarkOpacity > 1.0f
			? OPACITY_DEFAULT
			: watermarkOpacity;
	}

	public MeasureUnit getWatermarkRightUnitOrDefault() {
		return watermarkRightUnit == null ? UNIT_DEFAULT : watermarkRightUnit;
	}

	public MeasureUnit getWatermarkTopUnitOrDefault() {
		return watermarkTopUnit == null ? UNIT_DEFAULT : watermarkTopUnit;
	}

	public MeasureUnit getWatermarkWidthUnitOrDefault() {
		return watermarkWidthUnit == null ? UNIT_DEFAULT : watermarkWidthUnit;
	}

	public boolean isPublicDestinationOrDefault() {
		return publicDestination == null ? PUBLIC_DESTINATION_DEFAULT : publicDestination;
	}

	public boolean isRemoveSourceOnSuccessOrDefault() {
		return removeSourceOnSuccess == null ? REMOVE_SOURCE_ON_SUCCESS_DEFAULT : removeSourceOnSuccess;
	}

	public boolean isSkipIfPresentOrDefault() {
		return skipIfPresent == null ? SKIP_IF_PRESENT_DEFAULT : skipIfPresent;
	}
}
