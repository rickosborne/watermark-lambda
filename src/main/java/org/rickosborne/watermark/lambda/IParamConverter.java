package org.rickosborne.watermark.lambda;

@FunctionalInterface
public interface IParamConverter<T> {
	T convert(String value);
}
