package org.rickosborne.watermark.lambda;

import java.util.Optional;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConfigUtil {
	public static String environmentFromProperty(@NonNull final String name) {
		return name.toUpperCase().replace('.', '_');
	}

	public static Optional<Boolean> getBoolean(@NonNull final String propertyName) {
		return getString(propertyName)
			.map(Boolean::parseBoolean);
	}

	public static Optional<String> getEnvironmentString(@NonNull final String name) {
		return Optional.ofNullable(System.getenv(name))
			.filter(s -> !s.isBlank());
	}

	public static Optional<Float> getFloat(@NonNull final String propertyName) {
		return getString(propertyName)
			.map(Float::parseFloat);
	}

	public static Optional<Integer> getInteger(@NonNull final String propertyName) {
		return getString(propertyName)
			.map(Integer::parseUnsignedInt);
	}

	public static Optional<String> getPropertyString(@NonNull final String name) {
		return Optional.ofNullable(System.getProperty(name))
			.filter(s -> !s.isBlank());
	}

	public static Optional<String> getString(@NonNull final String propertyName) {
		final Optional<String> fromProp = getPropertyString(propertyName);
		if (fromProp.isPresent()) {
			return fromProp;
		}
		return getEnvironmentString(environmentFromProperty(propertyName));
	}

	public static Optional<String> getString(
		@NonNull final String propertyName,
		@NonNull final String envName
	) {
		final Optional<String> fromProp = getPropertyString(propertyName);
		if (fromProp.isPresent()) {
			return fromProp;
		}
		return getEnvironmentString(envName);
	}
}
