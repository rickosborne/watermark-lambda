package org.rickosborne.watermark.lambda;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import static org.rickosborne.watermark.lambda.ConfigUtil.getBoolean;
import static org.rickosborne.watermark.lambda.ConfigUtil.getFloat;
import static org.rickosborne.watermark.lambda.ConfigUtil.getInteger;
import static org.rickosborne.watermark.lambda.ConfigUtil.getString;

@UtilityClass
public class Configurer {
	public static <T> T configure(@NonNull final T obj) {
		for (final Field field : obj.getClass().getDeclaredFields()) {
			final Param param = field.getAnnotation(Param.class);
			final ConvertedParam convertedParam = field.getAnnotation(ConvertedParam.class);
			final String prop;
			final Class<? extends IParamConverter<?>> converterType;
			if (param == null && convertedParam == null) {
				continue;
			} else {
				prop = nullIfBlank(convertedParam == null ? param.value() : convertedParam.property());
				converterType = convertedParam == null ? null : convertedParam.converter();
				if (converterType != null && converterType.equals(IParamConverter.class)) {
					log("Missing converter for " + obj.getClass().getSimpleName() + "." + field.getName());
					continue;
				}
			}
			final String env = prop.toUpperCase().replace(".", "_");
			Object value = null;
			if (field.getType() == String.class) {
				value = getString(prop, env).orElse(null);
			} else if (field.getType() == Integer.class || field.getType() == int.class) {
				value = getInteger(prop).orElse(null);
			} else if (field.getType() == Boolean.class || field.getType() == boolean.class) {
				value = getBoolean(prop).orElse(null);
			} else if (field.getType() == Float.class || field.getType() == float.class) {
				value = getFloat(prop).orElse(null);
			} else if (converterType != null) {
				try {
					final IParamConverter<?> converter = converterType.getDeclaredConstructor().newInstance();
					final String string = getString(prop, env).orElse(null);
					if (string != null) {
						value = converter.convert(string);
					}
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					log("Could not instantiate: " + converterType.getSimpleName());
				}
			} else {
				log("No converter for: " + obj.getClass().getSimpleName() + "." + field.getName());
			}
			if (value != null) {
				try {
					field.setAccessible(true);
					field.set(obj, value);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return obj;
	}

	public static void log(final String message) {
		System.getLogger(Configurer.class.getCanonicalName()).log(System.Logger.Level.ERROR, message);
	}

	public static String nullIfBlank(final String s) {
		return s == null || s.isBlank() ? null : s.trim();
	}
}
