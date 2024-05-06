package com.ak.numbers;

import com.ak.util.Strings;
import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import javax.annotation.Nonnegative;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum Interpolators {
  AKIMA(new AkimaSplineInterpolator(), 5), LINEAR(new LinearInterpolator(), 2);

  private static final int SPLINE_POINTS = 100;
  private final UnivariateInterpolator interpolator;
  @Nonnegative
  private final int minPoints;

  Interpolators(UnivariateInterpolator interpolator, @Nonnegative int minPoints) {
    this.interpolator = Objects.requireNonNull(interpolator);
    this.minPoints = minPoints;
  }

  public static <C extends Enum<C> & Coefficients> Supplier<IntBinaryOperator> interpolator(Class<C> coeffEnum) {
    Map<C, IntUnaryOperator> coeffSplineMap = EnumSet.allOf(coeffEnum).stream().collect(
        Collectors.toMap(Function.identity(), coefficients -> interpolator(coefficients).get())
    );

    int limitX = RangeUtils.rangeX(coeffEnum).getMax();
    int limitY = IntStream.rangeClosed(0, limitX).map(x ->
        coeffSplineMap.values().stream().mapToInt(value -> value.applyAsInt(x)).summaryStatistics().getMax()
    ).summaryStatistics().getMax();

    IntFunction<int[]> samples = limit -> IntStream.concat(
        IntStream.iterate(0, value -> value < limit, operand -> operand + Math.max(1, limit / SPLINE_POINTS)),
        IntStream.of(limit)
    ).toArray();

    int[] xs = samples.apply(limitX);
    int[] ys = samples.apply(limitY);

    var z = new double[xs.length][ys.length];
    for (var i = 0; i < xs.length; i++) {
      int finalX = xs[i];
      List<C> sliceAlongY = coeffSplineMap.entrySet().stream().sorted(
          Comparator.comparingInt(o -> o.getValue().applyAsInt(finalX))
      ).map(Map.Entry::getKey).toList();

      double[] yValues = sliceAlongY.stream().mapToDouble(c -> coeffSplineMap.get(c).applyAsInt(finalX)).toArray();
      double[] zValues = sliceAlongY.stream().mapToDouble(c -> Double.parseDouble(Strings.numberSuffix(c.name()))).toArray();

      IntUnaryOperator operator = interpolator(yValues, zValues).get();
      for (var j = 0; j < ys.length; j++) {
        z[i][j] = operator.applyAsInt(ys[j]);
      }
    }
    return () -> new IntBinaryOperator() {
      private final BivariateFunction f = new PiecewiseBicubicSplineInterpolator().interpolate(
          IntStream.of(xs).mapToDouble(value -> value).toArray(), IntStream.of(ys).mapToDouble(value -> value).toArray(), z);

      @Override
      public int applyAsInt(int x, int y) {
        return (int) Math.round(f.value(Math.clamp(x, 0, xs[xs.length - 1]), Math.clamp(y, 0, ys[ys.length - 1])));
      }
    };
  }

  public static Supplier<IntUnaryOperator> interpolator(Coefficients coefficients) {
    double[][] pairs = coefficients.getPairs();
    return EnumSet.allOf(Interpolators.class).stream().filter(i -> pairs.length >= i.minPoints).findFirst().
        orElseThrow(() -> new IllegalArgumentException("Number of points %d from %s is too small".formatted(pairs.length, coefficients))).
        interpolate(pairs);
  }

  private static Supplier<IntUnaryOperator> interpolator(double[] abscissValues, double[] ordinateValues) {
    int length = Math.min(abscissValues.length, ordinateValues.length);
    return EnumSet.allOf(Interpolators.class).stream().filter(i -> length >= i.minPoints).findFirst().
        orElseThrow(() -> new IllegalArgumentException("Number of points %d is too small".formatted(length))).
        interpolate(abscissValues, ordinateValues);
  }

  private Supplier<IntUnaryOperator> interpolate(double[][] coefficients) {
    double[][] sorted = Arrays.stream(Objects.requireNonNull(coefficients)).sorted(Comparator.comparingDouble(o -> o[0]))
        .toArray(value -> new double[value][0]);
    var xValues = new double[sorted.length];
    var yValues = new double[sorted.length];

    for (var i = 0; i < xValues.length; i++) {
      xValues[i] = sorted[i][0];
      yValues[i] = sorted[i][1];
    }

    return interpolate(xValues, yValues);
  }

  private Supplier<IntUnaryOperator> interpolate(double[] xValues, double[] yValues) {
    return () -> new IntUnaryOperator() {
      private final UnivariateFunction f = interpolator.interpolate(xValues, yValues);

      @Override
      public int applyAsInt(int x) {
        return (int) Math.round(f.value(Math.clamp(x, xValues[0], xValues[xValues.length - 1])));
      }
    };
  }
}
