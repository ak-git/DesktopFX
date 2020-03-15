package com.ak.util;

import java.nio.file.Paths;
import java.util.function.BiFunction;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class LineFileBuilder<T> {
  @Nonnull
  private final String outFormat;
  @Nonnull
  private final Range xRange;
  @Nonnull
  private final Range yRange;
  @Nonnull
  private final MultiFileCollector.MultiFileCollectorBuilder<T> multiFileBuilder;

  private LineFileBuilder(@Nonnull String outFormat) {
    String[] formats = outFormat.split(" ");
    if (formats.length != 3) {
      throw new IllegalArgumentException(outFormat);
    }
    xRange = new Range(formats[0], LineFileCollector.Direction.HORIZONTAL);
    yRange = new Range(formats[1], LineFileCollector.Direction.VERTICAL);
    this.outFormat = formats[2];
    multiFileBuilder = new MultiFileCollector.MultiFileCollectorBuilder<>(this.outFormat);
  }

  public static <T> LineFileBuilder<T> of(@Nonnull String outFormat) {
    return new LineFileBuilder<>(outFormat);
  }

  public LineFileBuilder<T> xStream(Supplier<DoubleStream> doubleStreamSupplier) {
    xRange.doubleStreamSupplier = doubleStreamSupplier;
    return this;
  }

  public LineFileBuilder<T> xRange(double startInclusive, double endInclusive, @Nonnegative double step) {
    xRange.range(startInclusive, endInclusive, step);
    return this;
  }

  public LineFileBuilder<T> xLog10Range(double startInclusive, double endInclusive) {
    xRange.rangeLog10(startInclusive, endInclusive);
    return this;
  }

  public LineFileBuilder<T> yStream(Supplier<DoubleStream> doubleStreamSupplier) {
    yRange.doubleStreamSupplier = doubleStreamSupplier;
    return this;
  }

  public LineFileBuilder<T> yRange(double startInclusive, double endInclusive, @Nonnegative double step) {
    yRange.range(startInclusive, endInclusive, step);
    return this;
  }

  public LineFileBuilder<T> yLog10Range(double startInclusive, double endInclusive) {
    yRange.rangeLog10(startInclusive, endInclusive);
    return this;
  }

  public void generate(@Nonnull String fileName, @Nonnull DoubleBinaryOperator operator) {
    Supplier<DoubleStream> xVar = xRange::build;
    Supplier<DoubleStream> yVar = yRange::build;
    check(yVar.get().mapToObj(y -> xVar.get().map(x -> operator.applyAsDouble(x, y)))
        .map(stream -> stream.mapToObj(value -> String.format(outFormat, value)).collect(Collectors.joining(Strings.TAB)))
        .collect(new LineFileCollector(Paths.get(fileName), LineFileCollector.Direction.VERTICAL)));
  }

  public LineFileBuilder<T> add(@Nonnull String fileName, @Nonnull ToDoubleFunction<T> converter) {
    multiFileBuilder.add(Paths.get(fileName), converter);
    return this;
  }

  public void generate(@Nonnull BiFunction<Double, Double, T> doubleFunction) {
    Supplier<DoubleStream> xVar = xRange::build;
    Supplier<DoubleStream> yVar = yRange::build;
    check(yVar.get().mapToObj(y -> xVar.get().mapToObj(x -> doubleFunction.apply(x, y))).collect(multiFileBuilder.build()));
  }

  private static void check(boolean okFlag) {
    if (!okFlag) {
      throw new IllegalStateException();
    }
  }

  private static final class Range implements Builder<DoubleStream> {
    @Nonnull
    private final String outFormat;
    @Nonnull
    private final LineFileCollector.Direction direction;
    @Nonnull
    private Supplier<DoubleStream> doubleStreamSupplier = DoubleStream::empty;

    private Range(@Nonnull String outFormat, @Nonnull LineFileCollector.Direction direction) {
      this.outFormat = outFormat;
      this.direction = direction;
    }

    private void rangeLog10(@Nonnegative double start, @Nonnegative double end) {
      double from = Math.min(start, end);
      double to = Math.max(start, end);

      doubleStreamSupplier = () -> DoubleStream.concat(DoubleStream.iterate(from, value -> value < to, operand -> operand * 10.0).
          flatMap(scale -> DoubleStream.iterate(scale, value -> value < to, operand -> operand + scale / 5).limit(9 * 5L)), DoubleStream.of(to));
      toFile();
    }

    private void range(double start, double end, @Nonnegative double precision) {
      if (end <= start || (end - start < precision)) {
        throw new IllegalArgumentException(String.format(
            String.format("[%1$s .. %1$s] precision %1$s", outFormat), start, end, precision));
      }
      doubleStreamSupplier = () -> DoubleStream.iterate(start, value -> value < end + precision, dl2L -> dl2L + precision).sequential();
      toFile();
    }

    private void toFile() {
      String fileName = Extensions.TXT.attachTo(direction == LineFileCollector.Direction.HORIZONTAL ? "x" : "y");
      check(build().mapToObj(value -> String.format(outFormat, value)).collect(
          new LineFileCollector(Paths.get(fileName), direction)));
    }

    @Override
    public DoubleStream build() {
      return doubleStreamSupplier.get();
    }
  }
}
