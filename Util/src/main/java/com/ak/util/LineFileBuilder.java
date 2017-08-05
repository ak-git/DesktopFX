package com.ak.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.util.Builder;

public class LineFileBuilder {
  @Nonnull
  private final String[] outFormat;
  @Nullable
  private Range xRange;
  @Nullable
  private Range yRange;

  private LineFileBuilder(@Nonnull String outFormat) {
    this.outFormat = outFormat.split(" ");
    if (this.outFormat.length != 3) {
      throw new IllegalArgumentException(outFormat);
    }
  }

  public static LineFileBuilder of(@Nonnull String outFormat) {
    return new LineFileBuilder(outFormat);
  }

  public LineFileBuilder xRange(double startInclusive, double endInclusive, @Nonnegative double step) {
    xRange = new Range(startInclusive, endInclusive, step);
    return this;
  }

  public LineFileBuilder yRange(double startInclusive, double endInclusive, @Nonnegative double step) {
    yRange = new Range(startInclusive, endInclusive, step);
    return this;
  }

  public void generate(@Nonnull DoubleBinaryOperator operator) throws IOException {
    Supplier<DoubleStream> xVar = () -> Objects.requireNonNull(xRange).build();
    check(xVar.get().mapToObj(value -> String.format(outFormat[0], value)).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL)));

    Supplier<DoubleStream> yVar = () -> Objects.requireNonNull(yRange).build();
    check(yVar.get().mapToObj(value -> String.format(outFormat[1], value)).collect(
        new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));

    check(yVar.get().mapToObj(s2 -> xVar.get().map(s1 -> operator.applyAsDouble(s1, s2))).
        map(stream -> stream.mapToObj(value -> String.format(outFormat[2], value)).collect(Collectors.joining(Strings.TAB))).
        collect(new LineFileCollector(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  private static void check(@Nonnull Boolean errorFlag) {
    if (errorFlag) {
      throw new IllegalStateException();
    }
  }

  private static class Range implements Builder<DoubleStream> {
    private final double start;
    private final double end;
    @Nonnegative
    private final double precision;

    private Range(double start, double end, @Nonnegative double precision) {
      if (end <= start || (end - start < precision)) {
        throw new IllegalArgumentException(String.format("[%.6f .. %.6f] precision %.6f", start, end, precision));
      }
      this.start = start;
      this.end = end;
      this.precision = precision;
    }

    @Override
    public DoubleStream build() {
      return DoubleStream.iterate(start, dl2L -> dl2L + precision).
          limit(BigDecimal.valueOf((end - start) / precision + 1).
              round(MathContext.UNLIMITED).intValue()).sequential();
    }
  }
}
