package com.ak.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Paths;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javafx.util.Builder;
import org.testng.Assert;

public class LineFileBuilder {
  private final String outFormat;
  private Range xRange;
  private Range yRange;

  private LineFileBuilder(String outFormat) {
    this.outFormat = outFormat;
  }

  public void generate(DoubleBinaryOperator operator) throws IOException {
    Supplier<DoubleStream> xVar = () -> xRange.build();
    Assert.assertNull(xVar.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL)));

    Supplier<DoubleStream> yVar = () -> yRange.build();
    Assert.assertNull(yVar.get().mapToObj(value -> String.format("%.2f", value)).collect(
        new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));

    Assert.assertNull(yVar.get().mapToObj(s2 -> xVar.get().map(s1 -> operator.applyAsDouble(s1, s2))).
        map(stream -> stream.mapToObj(value -> String.format(outFormat, value)).collect(Collectors.joining(Strings.TAB))).
        collect(new LineFileCollector(Paths.get("z.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  public static LineFileBuilder of(String outFormat) {
    return new LineFileBuilder(outFormat);
  }

  public LineFileBuilder xRange(double startInclusive, double endInclusive, double step) {
    xRange = new Range(startInclusive, endInclusive, step);
    return this;
  }

  public LineFileBuilder yRange(double startInclusive, double endInclusive, double step) {
    yRange = new Range(startInclusive, endInclusive, step);
    return this;
  }

  private static class Range implements Builder<DoubleStream> {
    private final double start;
    private final double end;
    private final double precision;

    private Range(double start, double end, double precision) {
      if (end <= start || (end - start <= precision)) {
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
