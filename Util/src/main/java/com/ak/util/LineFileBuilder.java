package com.ak.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.util.Builder;

public class LineFileBuilder {
  @Nonnull
  private final String outFormat;
  @Nonnull
  private final Range xRange;
  @Nonnull
  private final Range yRange;

  private LineFileBuilder(@Nonnull String outFormat) {
    String[] formats = outFormat.split(" ");
    if (formats.length != 3) {
      throw new IllegalArgumentException(outFormat);
    }
    xRange = new Range(formats[0], LineFileCollector.Direction.HORIZONTAL);
    yRange = new Range(formats[1], LineFileCollector.Direction.VERTICAL);
    this.outFormat = formats[2];
  }

  public static LineFileBuilder of(@Nonnull String outFormat) {
    return new LineFileBuilder(outFormat);
  }

  public LineFileBuilder xStream(Supplier<DoubleStream> doubleStreamSupplier) {
    xRange.doubleStreamSupplier = doubleStreamSupplier;
    return this;
  }

  public LineFileBuilder xRange(double startInclusive, double endInclusive, @Nonnegative double step) {
    xRange.range(startInclusive, endInclusive, step);
    return this;
  }

  public LineFileBuilder yStream(Supplier<DoubleStream> doubleStreamSupplier) {
    yRange.doubleStreamSupplier = doubleStreamSupplier;
    return this;
  }

  public LineFileBuilder yRange(double startInclusive, double endInclusive, @Nonnegative double step) {
    yRange.range(startInclusive, endInclusive, step);
    return this;
  }

  public void generate(@Nonnull DoubleBinaryOperator operator) throws IOException {
    Supplier<DoubleStream> xVar = xRange::build;
    Supplier<DoubleStream> yVar = yRange::build;
    check(yVar.get().mapToObj(y -> xVar.get().map(x -> operator.applyAsDouble(x, y))).
        map(stream -> stream.mapToObj(value -> String.format(outFormat, value)).collect(Collectors.joining(Strings.TAB))).
        collect(new LineFileCollector(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  private static void check(@Nonnull Boolean errorFlag) {
    if (errorFlag) {
      throw new IllegalStateException();
    }
  }

  private static final class Range implements Builder<DoubleStream> {
    @Nonnull
    private final String outFormat;
    @Nonnull
    private final LineFileCollector.Direction direction;
    private double start;
    private double end;
    @Nonnegative
    private double precision;
    @Nullable
    private Supplier<DoubleStream> doubleStreamSupplier;

    private Range(@Nonnull String outFormat, @Nonnull LineFileCollector.Direction direction) {
      this.outFormat = outFormat;
      this.direction = direction;
    }

    private void range(double start, double end, @Nonnegative double precision) {
      if (end <= start || (end - start < precision)) {
        throw new IllegalArgumentException(String.format(
            String.format("[%1$s .. %1$s] precision %1$s", outFormat), start, end, precision));
      }
      this.start = start;
      this.end = end;
      this.precision = precision;

      try {
        String fileName = direction == LineFileCollector.Direction.HORIZONTAL ? "x.txt" : "y.txt";
        check(build().mapToObj(value -> String.format(outFormat, value)).collect(
            new LineFileCollector(Paths.get(fileName), direction)));
      }
      catch (IOException e) {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
      }
    }

    @Override
    public DoubleStream build() {
      return Optional.ofNullable(doubleStreamSupplier).orElse(() ->
          DoubleStream.iterate(start, dl2L -> dl2L + precision).
              limit(BigDecimal.valueOf((end - start) / precision + 1).
                  round(MathContext.UNLIMITED).intValue()).sequential()).get();
    }
  }
}
