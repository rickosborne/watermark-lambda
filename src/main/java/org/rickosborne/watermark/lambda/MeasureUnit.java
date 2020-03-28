package org.rickosborne.watermark.lambda;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeasureUnit {
	PERCENT("%"),
	PIXELS("px"),
	;
	private final String specifier;

	public static MeasureUnit fromString(final String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		for (final MeasureUnit value : values()) {
			if (value.name().equals(s) || value.specifier.equals(s)) {
				return value;
			}
		}
		throw new IllegalArgumentException("Not a MeasureUnit");
	}

	public static class Converter implements IParamConverter<MeasureUnit> {
		@Override
		public MeasureUnit convert(final String value) {
			return fromString(value);
		}
	}
}
