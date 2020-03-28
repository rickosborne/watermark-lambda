package org.rickosborne.watermark.lambda;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WatermarkS3Store implements WatermarkStore {
	private final AmazonS3 s3;

	public WatermarkS3Store() {
		this(AmazonS3ClientBuilder.defaultClient());
	}

	@Override
	public void delete(
		@NonNull final String bucketName,
		@NonNull final String key,
		final WatermarkLogger logger
	) {
		s3.deleteObject(bucketName, key);
	}

	@Override
	public boolean exists(
		@NonNull final String bucketName,
		@NonNull final String key,
		final WatermarkLogger logger
	) {
		return s3.doesObjectExist(bucketName, key);
	}

	@Override
	public InputStream read(
		@NonNull final String bucketName,
		@NonNull final String key,
		final WatermarkLogger logger
	) {
		return s3.getObject(bucketName, key).getObjectContent();
	}

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
		final ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(contentType);
		if (contentLength != null) {
			metadata.setContentLength(contentLength);
		}
		final PutObjectRequest barePut = new PutObjectRequest(
			bucketName,
			key,
			new ByteArrayInputStream(outBytes.toByteArray()),
			metadata
		);
		final PutObjectRequest aclPut = makePublic != null && makePublic ? barePut.withCannedAcl(CannedAccessControlList.PublicRead) : barePut;
		s3.putObject(aclPut);
	}
}
