package org.rickosborne.watermark.lambda;

import com.amazonaws.services.s3.model.ObjectMetadata;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.rickosborne.watermark.lambda.TestUtil.randomString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class WatermarkPostHandlerTest extends AWatermarkTest {

	private TestablePostHandler buildHandler() {
		return new TestablePostHandler();
	}

	@Test
	public void handleRequestWorksWithGoodData() {
		final TestablePostHandler handler = buildHandler();
		final WatermarkStorage storage = handler.getStorage();
		final WatermarkPostRequest post = WatermarkPostRequest.builder()
			.sourceBucket(randomString())
			.sourceKey(randomString() + ".jpg")
			.watermarkBucketName(randomString())
			.watermarkBucketPath(randomString() + ".png")
			.watermarkRight(95)
			.watermarkRightUnit(MeasureUnit.PERCENT)
			.watermarkWidth(10)
			.watermarkWidthUnit(MeasureUnit.PERCENT)
			.watermarkBottom(95)
			.watermarkBottomUnit(MeasureUnit.PERCENT)
			.watermarkOpacity(0.5f)
			.destinationBucket(randomString())
			.destinationKey(randomString() + "/")
			.renameKeyLength(8)
			.removeSourceOnSuccess(true)
			.publicDestination(true)
			.build();
		final String expectedKey = post.getDestinationKey() + "NznIcFJD.jpeg";
		final Flag didStore = Flag.start(false, "did call storage.write");
		final Flag didExists = Flag.start(false, "did call storage.exists");
		final Flag didDelete = Flag.start(false, "did call storage.delete");
		doAnswer(inv -> {
			didDelete.setFlagged(true);
			return null;
		}).when(storage).delete(eq(post.getSourceBucket()), eq(post.getSourceKey()));
		doAnswer(inv -> {
			didExists.setFlagged(true);
			return false;
		}).when(storage).exists(eq(post.getDestinationBucket()), eq(expectedKey));
		doAnswer(inv -> getWatermarkStream())
			.when(storage).read(eq(post.getWatermarkBucketName()), eq(post.getWatermarkBucketPath()));
		doAnswer(inv -> getExampleJpegStream())
			.when(storage).read(eq(post.getSourceBucket()), eq(post.getSourceKey()));
		doAnswer(inv -> {
			final String actualKey = inv.getArgument(1);
			assertEquals(actualKey, expectedKey, "destination key");
			final ByteArrayOutputStream outBytes = inv.getArgument(2);
			final String writeImagePath = System.getenv("WRITE_IMAGE_PATH");
			if (writeImagePath != null && !writeImagePath.isBlank()) {
				final File outFile = new File(writeImagePath);
				outBytes.writeTo(new FileOutputStream(outFile));
			}
			assertNotNull(outBytes, "should have output bytes");
			assertTrue(outBytes.toByteArray().length > 750000, "Expected JPEG bytes > 700000");
			final Consumer<ObjectMetadata> metadataConfigurer = inv.getArgument(3);
			final ObjectMetadata metadata = new ObjectMetadata();
			metadataConfigurer.accept(metadata);
			assertEquals(metadata.getContentType(), "image/jpeg");
			didStore.setFlagged(true);
			return null;
		}).when(storage).write(eq(post.getDestinationBucket()), any(), any(), any(), eq(true));
		final TestableAwsContext context = new TestableAwsContext();
		final WatermarkPostResponse response = handler.handleRequest(post, context);
		didStore.assertTrue();
		didExists.assertTrue();
		didDelete.assertTrue();
		final List<TestableImageProcessor.WatermarkCall> watermarkCalls = handler.getImageProcessor().getWatermarkCalls();
		assertEquals(watermarkCalls.size(), 1, "watermarkCalls should not be empty");
		final Rectangle dest = watermarkCalls.iterator().next().getDestination();
		assertEquals(dest.height, 310, "dest.height");
		assertEquals(dest.width, 330, "dest.width");
		assertEquals(dest.x, 2805, "dest.x");
		assertEquals(dest.y, 2113, "dest.y");
		assertNotNull(response, "Response should not be null");
		assertNull(response.getErrorMessage(), "null errorMessage");
		assertNull(response.getFailureReason(), "null failureReason");
		assertEquals(response.getDestinationKey(), expectedKey, "destinationKey");
		assertTrue(response.isSuccess(), "isSuccess");
	}

	@Getter
	private static class TestablePostHandler extends WatermarkPostHandler {
		private final WatermarkConfig config;
		private final TestableImageProcessor imageProcessor;
		private final WatermarkStorage storage;

		public TestablePostHandler() {
			this(buildWatermarkConfig());
		}

		public TestablePostHandler(@NonNull final WatermarkConfig config) {
			this(config, new TestableImageProcessor(), mock(WatermarkStorage.class));
		}

		public TestablePostHandler(
			@NonNull final WatermarkConfig config,
			@NonNull final TestableImageProcessor imageProcessor,
			@NonNull final WatermarkStorage storage
		) {
			super(config, imageProcessor, storage);
			this.storage = storage;
			this.config = config;
			this.imageProcessor = imageProcessor;
		}
	}
}