package org.rickosborne.watermark.lambda;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;

import static org.rickosborne.watermark.lambda.ConfigUtil.getString;

public class WatermarkFileStore implements WatermarkStore {
	public static final String STORE_FILE_ROOT_PARAM = "store.file.root";
	private final String root;
	private final Path rootPath;

	public WatermarkFileStore() {
		this(getOrCreateRoot());
	}

	public WatermarkFileStore(
		@NonNull final Path root
	) {
		rootPath = root.toAbsolutePath();
		this.root = rootPath.toString();
	}

	/**
	 * Note that this doesn't attempt to do a recursive delete, and thus may end up leaving garbage.
	 */
	@SneakyThrows(IOException.class)
	public static Path createTempDirectory() {
		final String prefix = WatermarkFileStore.class.getSimpleName();
		final Path tempDirectory = Files.createTempDirectory(prefix);
		tempDirectory.toFile().deleteOnExit();
		return tempDirectory;
	}

	public static Path getFileRootFromConfig() {
		return getString(STORE_FILE_ROOT_PARAM)
			.map(Path::of)
			.orElse(null);
	}

	public static Path getOrCreateRoot() {
		return Optional.ofNullable(getFileRootFromConfig())
			.orElseGet(WatermarkFileStore::createTempDirectory);
	}

	@Override
	public void delete(
		@NonNull final String bucketName,
		@NonNull final String key,
		final WatermarkLogger logger
	) {
		final Path keyPath = pathForKey(bucketName, key, true, logger);
		if (keyPath != null) {
			if (!keyPath.toFile().delete() && logger != null) {
				logger.error("Could not delete: " + keyPath);
			}
		}
	}

	@Override
	public boolean exists(
		@NonNull final String bucketName,
		@NonNull final String key,
		final WatermarkLogger logger
	) {
		return pathForKey(bucketName, key, true, logger) != null;
	}

	private Path pathForBucket(
		@NonNull final String bucketName,
		final boolean mustExist,
		final WatermarkLogger logger
	) {
		final Path path = rootPath.resolve(bucketName).toAbsolutePath();
		final File file = path.toFile();
		if (!file.toString().startsWith(root)) {
			if (logger != null) {
				logger.error("Bucket specifier tries to jailbreak: " + bucketName);
			}
			return null;
		}
		if (!file.exists()) {
			return mustExist ? null : path;
		}
		if (!file.isDirectory() || !file.canRead()) {
			if (logger != null) {
				logger.error("Bucket is not a file, or not readable: " + bucketName);
			}
			return null;
		}
		return path;
	}

	private Path pathForKey(
		@NonNull final String bucketName,
		@NonNull final String key,
		final boolean mustExist,
		final WatermarkLogger logger
	) {
		final File bucketPath = Optional.ofNullable(pathForBucket(bucketName, true, logger))
			.map(Path::toFile)
			.orElse(null);
		if (bucketPath != null) {
			final Path keyPath = bucketPath.toPath().resolve(key).toAbsolutePath();
			if (!keyPath.toString().startsWith(bucketPath.toString())) {
				if (logger != null) {
					logger.error("Key specifier tries to jailbreak: " + key);
				}
				return null;
			}
			final File keyFile = keyPath.toFile();
			if (!keyFile.exists()) {
				if (mustExist) {
					if (logger != null) {
						logger.error("File does not exist: " + keyFile);
					}
					return null;
				}
				return keyPath;
			}
			if (!keyFile.isFile() || !keyFile.canRead()) {
				if (logger != null) {
					logger.error("Not a file: " + keyPath);
				}
				return null;
			}
			return keyPath;
		} else {
			if (logger != null) {
				logger.error("Invalid bucket: " + bucketName);
			}
			return null;
		}
	}

	@Override
	public InputStream read(
		@NonNull final String bucketName,
		@NonNull final String key,
		final WatermarkLogger logger
	) {
		final Path keyPath = pathForKey(bucketName, key, true, logger);
		if (keyPath != null) {
			try {
				return new FileInputStream(keyPath.toFile());
			} catch (FileNotFoundException e) {
				if (logger != null) {
					logger.error("File not found: " + keyPath);
				}
			}
		}
		return null;
	}

	@SneakyThrows(IOException.class)
	@Override
	public void write(
		@NonNull final String bucketName,
		@NonNull final String key,
		@NonNull final ByteArrayOutputStream outBytes,
		@NonNull final String contentType,
		final Long contentLength,
		final Boolean makePublic,
		final WatermarkLogger logger
	) {
		final Path path = pathForKey(bucketName, key, false, logger);
		if (path != null) {
			Files.createDirectories(path.getParent());
			final FileOutputStream out = new FileOutputStream(path.toFile(), false);
			outBytes.writeTo(out);
			out.close();
			if (logger != null) {
				logger.debug("Wrote " + bucketName + ":" + key);
			}
		}
	}
}
