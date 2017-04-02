package com.ak.numbers;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import com.ak.util.Strings;
import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

public enum Interpolators {
  AKIMA(new AkimaSplineInterpolator(), 5), LINEAR(new LinearInterpolator(), 2);

  @Nonnull
  private final UnivariateInterpolator interpolator;
  private final int minPoints;

  Interpolators(@Nonnull UnivariateInterpolator interpolator, int minPoints) {
    this.interpolator = interpolator;
    this.minPoints = minPoints;
  }

  public static Provider<IntBinaryOperator> interpolator(@Nonnull Coefficients[] xyForZ) {
    Map<Coefficients, IntUnaryOperator> coeffSplineMap = Stream.of(xyForZ).collect(
        Collectors.toMap(Function.identity(), coefficients -> interpolator(coefficients).get())
    );

    int limitX = (int) Math.floor(coeffSplineMap.keySet().stream().mapToDouble(c -> {
      double[][] pairs = c.getPairs();
      return pairs[pairs.length - 1][0];
    }).summaryStatistics().getMax());

    int limitY = IntStream.rangeClosed(0, limitX).map(x ->
        coeffSplineMap.values().stream().mapToInt(value -> value.applyAsInt(x)).summaryStatistics().getMax()
    ).summaryStatistics().getMax();


    double[] xs = DoubleStream.iterate(0, operand -> operand + Math.max(1, limitX / 100)).limit(102).toArray();
    double[] ys = DoubleStream.iterate(0, operand -> operand + Math.max(1, limitY / 100)).limit(102).toArray();

    double[][] z = new double[xs.length][ys.length];
    for (int i = 0; i < xs.length; i++) {
      int finalX = (int) xs[i];
      List<Coefficients> sliceAlongY = coeffSplineMap.entrySet().stream().sorted(
          Comparator.comparingInt(o -> o.getValue().applyAsInt(finalX))
      ).map(Map.Entry::getKey).collect(Collectors.toList());

      double[] yValues = sliceAlongY.stream().mapToDouble(c -> coeffSplineMap.get(c).applyAsInt(finalX)).toArray();
      double[] zValues = sliceAlongY.stream().mapToDouble(c -> Double.parseDouble(c.name().replaceAll("\\D*", Strings.EMPTY))).toArray();

      IntUnaryOperator operator = interpolator(yValues, zValues).get();
      for (int j = 0; j < ys.length; j++) {
        z[i][j] = operator.applyAsInt((int) ys[j]);
      }
    }
    return () -> new IntBinaryOperator() {
      private final BivariateFunction f = new PiecewiseBicubicSplineInterpolator().interpolate(xs, ys, z);

      @Override
      public int applyAsInt(int x, int y) {
        return (int) Math.round(f.value(Math.min(Math.max(x, 0), limitX), Math.min(Math.max(y, 0), limitY)));
      }
    };
  }

  public static Provider<IntUnaryOperator> interpolator(@Nonnull Coefficients coefficients) {
    double[][] pairs = coefficients.getPairs();
    return EnumSet.allOf(Interpolators.class).stream().filter(i -> pairs.length >= i.minPoints).findFirst().
        orElseThrow(() -> new IllegalArgumentException(String.format("Number of points %d from %s is too small", pairs.length, coefficients.name()))).
        interpolate(pairs);
  }

  private static Provider<IntUnaryOperator> interpolator(@Nonnull double[] xValues, @Nonnull double[] yValues) {
    int length = Math.min(xValues.length, yValues.length);
    return EnumSet.allOf(Interpolators.class).stream().filter(i -> length >= i.minPoints).findFirst().
        orElseThrow(() -> new IllegalArgumentException(String.format("Number of points %d is too small", length))).
        interpolate(xValues, yValues);
  }

  private Provider<IntUnaryOperator> interpolate(@Nonnull double[][] coefficients) {
    double[] xValues = new double[coefficients.length];
    double[] yValues = new double[coefficients.length];

    for (int i = 0; i < xValues.length; i++) {
      xValues[i] = coefficients[i][0];
      yValues[i] = coefficients[i][1];
    }

    return interpolate(xValues, yValues);
  }

  private Provider<IntUnaryOperator> interpolate(@Nonnull double[] xValues, @Nonnull double[] yValues) {
    return () -> new IntUnaryOperator() {
      private final UnivariateFunction f = interpolator.interpolate(xValues, yValues);

      @Override
      public int applyAsInt(int x) {
        return (int) Math.round(f.value(Math.min(Math.max(x, xValues[0]), xValues[xValues.length - 1])));
      }
    };
  }
}
