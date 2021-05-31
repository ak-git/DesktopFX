package com.ak.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CSVLineFileBuilder<T> {
  private final Range xRange = new Range();
  private final Range yRange = new Range();
  @Nonnull
  private final BiFunction<Double, Double, T> doubleFunction;
  @Nullable
  private CSVMultiFileCollector.Builder<Double, T> multiFileBuilder;

  private CSVLineFileBuilder(@Nonnull BiFunction<Double, Double, T> doubleFunction) {
    this.doubleFunction = doubleFunction;
  }

  public static <T> CSVLineFileBuilder<T> of(@Nonnull BiFunction<Double, Double, T> doubleFunction) {
    return new CSVLineFileBuilder<>(doubleFunction);
  }

  public CSVLineFileBuilder<T> xStream(Supplier<DoubleStream> doubleStreamSupplier) {
    xRange.doubleStreamSupplier = doubleStreamSupplier;
    return this;
  }

  public CSVLineFileBuilder<T> xRange(double startInclusive, double endInclusive, @Nonnegative double step) {
    xRange.range(startInclusive, endInclusive, step);
    return this;
  }

  public CSVLineFileBuilder<T> xLog10Range(double startInclusive, double endInclusive) {
    xRange.rangeLog10(startInclusive, endInclusive);
    return this;
  }

  public CSVLineFileBuilder<T> yStream(Supplier<DoubleStream> doubleStreamSupplier) {
    yRange.doubleStreamSupplier = doubleStreamSupplier;
    return this;
  }

  public CSVLineFileBuilder<T> yRange(double startInclusive, double endInclusive, @Nonnegative double step) {
    yRange.range(startInclusive, endInclusive, step);
    return this;
  }

  public CSVLineFileBuilder<T> yLog10Range(double startInclusive, double endInclusive) {
    yRange.rangeLog10(startInclusive, endInclusive);
    return this;
  }

  public CSVLineFileBuilder<T> saveTo(@Nonnull String fileName, @Nonnull Function<T, Object> converter) {
    if (multiFileBuilder == null) {
      multiFileBuilder = new CSVMultiFileCollector.Builder<>(
          yRange.build().boxed(),
          Stream.concat(Stream.of(Strings.EMPTY), xRange.build().mapToObj(Double::toString)).toArray(String[]::new)
      );
    }
    multiFileBuilder.add(Paths.get(Extension.CSV.attachTo(fileName)), converter);
    return this;
  }

  public void generate() {
    Supplier<DoubleStream> xVar = xRange::build;
    Supplier<DoubleStream> yVar = yRange::build;
    Objects.requireNonNull(
        yVar.get()
            .mapToObj(
                y -> xVar.get().mapToObj(x -> doubleFunction.apply(x, y))
            )
            .collect(Objects.requireNonNull(multiFileBuilder).build())
    );
  }

  private static final class Range implements Builder<DoubleStream> {
    @Nonnull
    private Supplier<DoubleStream> doubleStreamSupplier = DoubleStream::empty;

    private void rangeLog10(@Nonnegative double start, @Nonnegative double end) {
      double from = Math.min(start, end);
      double to = Math.max(start, end);

      doubleStreamSupplier = () ->
          DoubleStream.concat(
              DoubleStream.iterate(from, value -> value < to, operand -> operand * 10.0)
                  .flatMap(scale -> {
                        double step = scale / 5.0;
                        return DoubleStream
                            .iterate(scale, value -> value < to - scale / 10.0, operand -> operand + step)
                            .limit(9 * 5L).map(round(step).get());
                      }
                  ),
              DoubleStream.of(to)
          );
    }

    private void range(double start, double end, @Nonnegative double step) {
      doubleStreamSupplier = () ->
          DoubleStream.iterate(start, value -> value < end + step / 2.0, dl2L -> dl2L + step).map(round(step).get());
    }

    private static Supplier<DoubleUnaryOperator> round(@Nonnegative double step) {
      int afterZero = (int) -Math.floor(StrictMath.log10(step));
      return () -> x -> BigDecimal.valueOf(x).setScale(afterZero, RoundingMode.HALF_EVEN).doubleValue();
    }

    @Override
    public DoubleStream build() {
      return doubleStreamSupplier.get();
    }
  }
}
