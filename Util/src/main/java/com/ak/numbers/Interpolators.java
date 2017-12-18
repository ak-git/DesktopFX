package com.ak.numbers;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
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

  private static final int SPLINE_POINTS = 100;
  @Nonnull
  private final UnivariateInterpolator interpolator;
  @Nonnegative
  private final int minPoints;

  Interpolators(@Nonnull UnivariateInterpolator interpolator, @Nonnegative int minPoints) {
    this.interpolator = interpolator;
    this.minPoints = minPoints;
  }

  public static <C extends Enum<C> & Coefficients> Provider<IntBinaryOperator> interpolator(@Nonnull Class<C> coeffEnum) {
    Map<Coefficients, IntUnaryOperator> coeffSplineMap = EnumSet.allOf(coeffEnum).stream().collect(
        Collectors.toMap(Function.identity(), coefficients -> interpolator(coefficients).get())
    );

    int limitX = CoefficientsUtils.rangeX(coeffEnum).getMax();
    int limitY = IntStream.rangeClosed(0, limitX).map(x ->
        coeffSplineMap.values().stream().mapToInt(value -> value.applyAsInt(x)).summaryStatistics().getMax()
    ).summaryStatistics().getMax();

    IntFunction<int[]> samples = limit -> IntStream.concat(
        IntStream.iterate(0, operand -> operand + Math.max(1, limit / SPLINE_POINTS)).takeWhile(value -> value < limit),
        IntStream.of(limit)
    ).toArray();

    int[] xs = samples.apply(limitX);
    int[] ys = samples.apply(limitY);

    double[][] z = new double[xs.length][ys.length];
    for (int i = 0; i < xs.length; i++) {
      int finalX = xs[i];
      List<Coefficients> sliceAlongY = coeffSplineMap.entrySet().stream().sorted(
          Comparator.comparingInt(o -> o.getValue().applyAsInt(finalX))
      ).map(Map.Entry::getKey).collect(Collectors.toList());

      double[] yValues = sliceAlongY.stream().mapToDouble(c -> coeffSplineMap.get(c).applyAsInt(finalX)).toArray();
      double[] zValues = sliceAlongY.stream().mapToDouble(c -> Double.parseDouble(c.name().replaceAll(".*_", Strings.EMPTY))).toArray();

      IntUnaryOperator operator = interpolator(yValues, zValues).get();
      for (int j = 0; j < ys.length; j++) {
        z[i][j] = operator.applyAsInt(ys[j]);
      }
    }
    return () -> new IntBinaryOperator() {
      private final BivariateFunction f = new PiecewiseBicubicSplineInterpolator().interpolate(
          IntStream.of(xs).mapToDouble(value -> value).toArray(), IntStream.of(ys).mapToDouble(value -> value).toArray(), z);

      @Override
      public int applyAsInt(int x, int y) {
        return (int) Math.round(f.value(Math.min(Math.max(x, 0), xs[xs.length - 1]), Math.min(Math.max(y, 0), ys[ys.length - 1])));
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
