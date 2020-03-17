package org.rickosborne.watermark.lambda;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

public class WatermarkBoundsCalculator {
	public Rectangle getBounds(
		@NonNull final WatermarkRequest request,
		@NonNull final BufferedImage sourceImage,
		@NonNull final BufferedImage watermarkImage
	) {
		final WatermarkPostRequest requestConfig = request.getRequestConfig();
		final Registers registers = new Registers(
			((float) watermarkImage.getWidth()) / ((float) watermarkImage.getHeight()),
			getPixels(requestConfig.getWatermarkBottom(), requestConfig.getWatermarkBottomUnitOrDefault(), sourceImage.getHeight()),
			getPixels(requestConfig.getWatermarkHeight(), requestConfig.getWatermarkHeightUnitOrDefault(), sourceImage.getHeight()),
			getPixels(requestConfig.getWatermarkLeft(), requestConfig.getWatermarkLeftUnitOrDefault(), sourceImage.getWidth()),
			getPixels(requestConfig.getWatermarkRight(), requestConfig.getWatermarkRightUnitOrDefault(), sourceImage.getWidth()),
			getPixels(requestConfig.getWatermarkTop(), requestConfig.getWatermarkTopUnitOrDefault(), sourceImage.getHeight()),
			getPixels(requestConfig.getWatermarkWidth(), requestConfig.getWatermarkWidthUnitOrDefault(), sourceImage.getWidth())
		);
		final List<Calculable> calculables = new CopyOnWriteArrayList<>(Arrays.asList(Calculable.values()));
		while (true) {
			boolean anyApplied = false;
			for (final Calculable calculable : calculables) {
				final CalcResult calcResult = calculable.calculate(registers);
				if (calcResult == CalcResult.ALREADY_DONE) {
					calculables.remove(calculable);
				} else if (calcResult == CalcResult.APPLIED) {
					anyApplied = true;
					calculables.remove(calculable);
					break;
				}
			}
			if (calculables.isEmpty()) {
				return new Rectangle(registers.left, registers.top, registers.width, registers.height);
			} else if (!anyApplied) {
				return null;
			}
		}
	}

	private Integer getPixels(final Integer num, final MeasureUnit unit, final int scale) {
		return Optional.ofNullable(num)
			.map(n -> unit == MeasureUnit.PIXELS ? Math.min(n, scale) : Math.round(((float) scale) * ((float) n * 0.01f)))
			.orElse(null);
	}

	private enum CalcResult {
		ALREADY_DONE,
		APPLIED,
		MISSING_OPERANDS,
	}

	@SuppressWarnings("unused")
	private enum Calculable {
		WIDTH_FROM_RIGHT_LEFT(Registers::getWidth, Registers::setWidth, Registers::getRight, Registers::getLeft, (r, l) -> r - l),
		HEIGHT_FROM_BOTTOM_TOP(Registers::getHeight, Registers::setHeight, Registers::getBottom, Registers::getTop, (b, t) -> b - t),
		LEFT_FROM_RIGHT_WIDTH(Registers::getLeft, Registers::setLeft, Registers::getRight, Registers::getWidth, (r, w) -> r - w),
		TOP_FROM_BOTTOM_HEIGHT(Registers::getTop, Registers::setTop, Registers::getBottom, Registers::getHeight, (b, h) -> b - h),
		BOTTOM_FROM_TOP_HEIGHT(Registers::getBottom, Registers::setBottom, Registers::getTop, Registers::getHeight, Integer::sum),
		RIGHT_FROM_LEFT_WIDTH(Registers::getRight, Registers::setRight, Registers::getLeft, Registers::getWidth, Integer::sum),
		// These should be toward the bottom
		WIDTH_FROM_HEIGHT(Registers::getWidth, Registers::setWidth, Registers::getHeight, Registers::getAspectRatio, (h, ar) -> Math.round(h * ar)),
		HEIGHT_FROM_WIDTH(Registers::getHeight, Registers::setHeight, Registers::getWidth, Registers::getAspectRatio, (w, ar) -> Math.round(w / ar)),
		;
		private final Function<Registers, Integer> destGetter;
		private final Function<Registers, Integer> op1getter;
		private final Function<Registers, Object> op2getter;
		private final BiFunction<Integer, Object, Integer> operation;
		private final BiConsumer<Registers, Integer> resultSetter;

		@SuppressWarnings("unchecked")
		<T> Calculable(
			final Function<Registers, Integer> destGetter,
			final BiConsumer<Registers, Integer> resultSetter,
			final Function<Registers, Integer> op1getter,
			final Function<Registers, T> op2getter,
			final BiFunction<Integer, T, Integer> operation
		) {
			this.destGetter = destGetter;
			this.resultSetter = resultSetter;
			this.op1getter = op1getter;
			this.op2getter = (Function<Registers, Object>) op2getter;
			this.operation = (BiFunction<Integer, Object, Integer>) operation;
		}

		public CalcResult calculate(@NonNull final Registers registers) {
			final Integer existing = destGetter.apply(registers);
			if (existing != null) {
				return CalcResult.ALREADY_DONE;
			}
			final Integer op1 = op1getter.apply(registers);
			if (op1 == null) {
				return CalcResult.MISSING_OPERANDS;
			}
			final Object op2 = op2getter.apply(registers);
			if (op2 == null) {
				return CalcResult.MISSING_OPERANDS;
			}
			final Integer result = operation.apply(op1, op2);
			resultSetter.accept(registers, result);
			return CalcResult.APPLIED;
		}
	}

	@Data
	@AllArgsConstructor
	private static class Registers {
		float aspectRatio;
		Integer bottom;
		Integer height;
		Integer left;
		Integer right;
		Integer top;
		Integer width;
	}
}
