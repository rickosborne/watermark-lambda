package org.rickosborne.watermark.lambda;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConvertedParam {
	Class<? extends IParamConverter<?>> converter();

	String property();
}
