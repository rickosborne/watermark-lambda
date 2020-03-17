package org.rickosborne.watermark.lambda;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WatermarkStorage {
	private final AmazonS3 s3;

	public WatermarkStorage() {
		this(AmazonS3ClientBuilder.defaultClient());
	}

	public void delete(
		@NonNull final String bucketName,
		@NonNull final String key
	) {
		s3.deleteObject(bucketName, key);
	}

	public boolean exists(
		@NonNull final String bucketName,
		@NonNull final String key
	) {
		return s3.doesObjectExist(bucketName, key);
	}

	public InputStream read(
		@NonNull final String bucketName,
		@NonNull final String key
	) {
		return s3.getObject(bucketName, key).getObjectContent();
	}

	public void write(
		@NonNull final String bucketName,
		@NonNull final String key,
		@NonNull final ByteArrayOutputStream outBytes,
		@NonNull final Consumer<ObjectMetadata> metadataConfigurer,
		final boolean makePublic
	) {
		final ObjectMetadata metadata = new ObjectMetadata();
		metadataConfigurer.accept(metadata);
		final PutObjectRequest barePut = new PutObjectRequest(
			bucketName,
			key,
			new ByteArrayInputStream(outBytes.toByteArray()),
			metadata
		);
		final PutObjectRequest aclPut = makePublic ? barePut.withCannedAcl(CannedAccessControlList.PublicRead) : barePut;
		s3.putObject(aclPut);
	}
}
