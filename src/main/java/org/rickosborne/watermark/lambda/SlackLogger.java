package org.rickosborne.watermark.lambda;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.slack.api.Slack;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SlackLogger {
	private final String appId;
	private final String botOauth;
	private final String clientId;
	private final LambdaLogger lambdaLogger;
	private final String notificationChannel;
	private final String oauth;
	private final String signingSecret;
	private final String token;
	private final String verificationToken;

	public static SlackLogger fromConfig(final WatermarkConfig config, final LambdaLogger lambdaLogger) {
		if (config == null) {
			return null;
		}
		return new SlackLogger(
			config.getSlackAppId(),
			config.getSlackBotOauth(),
			config.getSlackClientId(),
			lambdaLogger,
			config.getSlackNotificationChannel(),
			config.getSlackOauth(),
			config.getSlackSigningSecret(),
			config.getSlackToken(),
			config.getSlackVerificationToken()
		);
	}

	public void send(final String message, final boolean markdown) {
		if (botOauth == null || notificationChannel == null) {
			return;
		}
		try (final Slack slack = Slack.getInstance()) {
			final ChatPostMessageResponse resp = slack.methods(botOauth).chatPostMessage(req -> req
				.channel(notificationChannel)
				.mrkdwn(markdown)
				.text(message)
			);
			if (lambdaLogger != null) {
				if (resp.isOk()) {
					lambdaLogger.log("Sent message to Slack: " + message);
				} else {
					lambdaLogger.log("Sent message to Slack: " + message + " !! " + resp.getError());
				}
			}
		} catch (Exception e) {
			if (lambdaLogger != null) {
				lambdaLogger.log("Failed to send message to Slack: " + e.getClass().getSimpleName() + " " + e.getMessage());
			}
		}
	}
}
