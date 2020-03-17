package org.rickosborne.watermark.lambda;

import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Value;

@Getter
@Setter
public class TestableStorage extends WatermarkStorage {
	private InputStream nextRead;
	private final List<ReadCall> reads = new LinkedList<>();
	private final List<WriteCall> writes = new LinkedList<>();

	public TestableStorage() {
		super(null);
	}

	@Override
	public InputStream read(@NonNull final String bucketName, @NonNull final String key) {
		reads.add(new ReadCall(bucketName, key));
		return nextRead;
	}

	@Override
	public void write(
		final @NonNull String bucketName,
		final @NonNull String key,
		final @NonNull ByteArrayOutputStream outBytes,
		final @NonNull Consumer<ObjectMetadata> metadataConfigurer,
		final boolean makePublic
	) {
		final ObjectMetadata metadata = new ObjectMetadata();
		metadataConfigurer.accept(metadata);
		writes.add(new WriteCall(bucketName, key, makePublic, metadata, outBytes));
	}

	@Value
	public static class ReadCall {
		String bucketName;
		String key;
	}

	@Value
	public static class WriteCall {
		String bucketName;
		String key;
		boolean makePublic;
		ObjectMetadata metadata;
		ByteArrayOutputStream outBytes;
	}
}
