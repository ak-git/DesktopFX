package com.ak.util;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public final class CSVLineFileBuilder<T> {
  private final Range xRange = new Range();
  private final Range yRange = new Range();
  @Nonnull
  private final CSVMultiFileCollector.Builder<T> multiFileBuilder;

  public CSVLineFileBuilder(@Nonnull String... headers) {
    multiFileBuilder = new CSVMultiFileCollector.Builder<>(headers);
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

  public CSVLineFileBuilder<T> add(@Nonnull String fileName, @Nonnull Function<T, Object> converter) {
    multiFileBuilder.add(fileName, converter);
    return this;
  }

  public void generate(@Nonnull BiFunction<Double, Double, T> doubleFunction) {
    Supplier<DoubleStream> xVar = xRange::build;
    Supplier<DoubleStream> yVar = yRange::build;
    Objects.requireNonNull(yVar.get().mapToObj(y -> xVar.get().mapToObj(x -> doubleFunction.apply(x, y))).collect(multiFileBuilder.build()));
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
                  .flatMap(scale ->
                      DoubleStream.iterate(scale, value -> value < to - scale / 10.0, operand -> operand + scale / 5.0)
                          .limit(9 * 5L)
                  ),
              DoubleStream.of(to)
          );
    }

    private void range(double start, double end, @Nonnegative double precision) {
      doubleStreamSupplier = () -> DoubleStream.iterate(start, value -> value < end + precision / 2.0, dl2L -> dl2L + precision).sequential();
    }

    @Override
    public DoubleStream build() {
      return doubleStreamSupplier.get();
    }
  }
}
