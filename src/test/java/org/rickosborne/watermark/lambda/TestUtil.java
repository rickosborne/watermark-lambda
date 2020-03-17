package org.rickosborne.watermark.lambda;

import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtil {
	public static String randomString() {
		return UUID.randomUUID().toString();
	}
}
