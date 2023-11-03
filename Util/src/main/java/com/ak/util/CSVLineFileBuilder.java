package com.ak.util;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static java.lang.StrictMath.log10;

public final class CSVLineFileBuilder<T> {
  private final Range xRange = new Range();
  private final Range yRange = new Range();
  @Nonnull
  private final BiFunction<Double, Double, T> doubleFunction;
  @Nullable
  private CSVMultiFileCollector.CollectorBuilder<Double, T> multiFileBuilder;

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

  public CSVLineFileBuilder<T> xLogRange(double startInclusive, double endInclusive) {
    xRange.rangeLog(startInclusive, endInclusive);
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

  public CSVLineFileBuilder<T> yLogRange(double startInclusive, double endInclusive) {
    yRange.rangeLog(startInclusive, endInclusive);
    return this;
  }

  public CSVLineFileBuilder<T> saveTo(@Nonnull String fileName, @Nonnull Function<T, Object> converter) {
    if (multiFileBuilder == null) {
      multiFileBuilder = new CSVMultiFileCollector.CollectorBuilder<>(
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

    @Override
    public DoubleStream build() {
      return doubleStreamSupplier.get();
    }

    private void rangeLog(@Nonnegative double start, @Nonnegative double end) {
      doubleStreamSupplier = () -> Numbers.rangeLog(start, end,
          Math.max(Math.abs(Math.round(log10(start / end))), Math.round(log10(Math.abs(start - end)))) * 10);
    }

    private void range(double start, double end, @Nonnegative double step) {
      doubleStreamSupplier = () ->
          DoubleStream.iterate(start, value -> value < end + step / 2.0, dl2L -> dl2L + step).map(Numbers.round(step));
    }
  }
}
