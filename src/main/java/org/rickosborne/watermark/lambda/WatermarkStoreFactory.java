package org.rickosborne.watermark.lambda;

import static org.rickosborne.watermark.lambda.ConfigUtil.getString;
import static org.rickosborne.watermark.lambda.WatermarkFileStore.STORE_FILE_ROOT_PARAM;

public class WatermarkStoreFactory {
	/**
	 * This is a little lame, but it works at the moment.
	 */
	public static WatermarkStore buildStore() {
		final String storeFileRoot = getString(STORE_FILE_ROOT_PARAM).orElse(null);
		if (storeFileRoot != null) {
			return new WatermarkFileStore();
		} else {
			return new WatermarkS3Store();
		}
	}
}
