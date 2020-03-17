package org.rickosborne.watermark.lambda;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.testng.Assert;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Flag {
	private final String description;
	private boolean flagged;

	public static Flag start(final boolean flagged, final String description) {
		return new Flag(description, flagged);
	}

	public void assertFalse() {
		Assert.assertFalse(flagged, description);
	}

	public void assertTrue() {
		Assert.assertTrue(flagged, description);
	}
}
