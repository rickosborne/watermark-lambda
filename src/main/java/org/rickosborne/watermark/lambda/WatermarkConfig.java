package org.rickosborne.watermark.lambda;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@Builder(toBuilder = true)
@NonFinal
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WatermarkConfig {
	@Param("s3.inbox.bucket")
	String inboxBucketName;

	@Param("s3.inbox.path")
	String inboxBucketPath;

	@Param("s3.outbox.bucket")
	String outboxBucketName;

	@Param("s3.outbox.path")
	String outboxBucketPath;

	@Param("s3.outbox.url")
	String outboxUrl;

	@Param("s3.outbox.public")
	Boolean publicDestination;

	@Param("s3.inbox.removeOnSuccess")
	Boolean removeSourceOnSuccess;

	@Param("rename.key.length")
	Integer renameKeyLength;

	@Param("s3.outbox.skipIfPresent")
	Boolean skipIfPresent;

	@Param("slack.appId")
	String slackAppId;

	@Param("slack.bot.oauth")
	String slackBotOauth;

	@Param("slack.clientId")
	String slackClientId;

	@Param("slack.notification.channel")
	String slackNotificationChannel;

	@Param("slack.oauth")
	String slackOauth;

	@Param("slack.signingSecret")
	String slackSigningSecret;

	@Param("slack.token")
	String slackToken;

	@Param("slack.verificationToken")
	String slackVerificationToken;

	@Param("watermark.bottom.value")
	Integer watermarkBottom;

	@ConvertedParam(property = "watermark.bottom.units", converter = MeasureUnit.Converter.class)
	MeasureUnit watermarkBottomUnit;

	@Param("s3.watermark.bucket")
	String watermarkBucketName;

	@Param("s3.watermark.path")
	String watermarkBucketPath;

	@Param("watermark.height.value")
	Integer watermarkHeight;

	@ConvertedParam(property = "watermark.height.units", converter = MeasureUnit.Converter.class)
	MeasureUnit watermarkHeightUnit;

	@Param("watermark.left.value")
	Integer watermarkLeft;

	@ConvertedParam(property = "watermark.left.units", converter = MeasureUnit.Converter.class)
	MeasureUnit watermarkLeftUnit;

	@Param("watermark.opacity")
	Float watermarkOpacity;

	@Param("watermark.right.value")
	Integer watermarkRight;

	@ConvertedParam(property = "watermark.right.units", converter = MeasureUnit.Converter.class)
	MeasureUnit watermarkRightUnit;

	@Param("watermark.top.value")
	Integer watermarkTop;

	@ConvertedParam(property = "watermark.top.units", converter = MeasureUnit.Converter.class)
	MeasureUnit watermarkTopUnit;

	@Param("watermark.width.value")
	Integer watermarkWidth;

	@ConvertedParam(property = "watermark.width.units", converter = MeasureUnit.Converter.class)
	MeasureUnit watermarkWidthUnit;

	public static WatermarkConfig build() {
		return Configurer.configure(WatermarkConfig.builder().build());
	}
}
