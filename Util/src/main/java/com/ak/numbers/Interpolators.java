package com.ak.numbers;

import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
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

  private Provider<IntUnaryOperator> interpolate(@Nonnull double[] coefficients) {
    double[] xValues = new double[coefficients.length / 2];
    double[] yValues = new double[coefficients.length / 2];

    for (int i = 0; i < xValues.length; i++) {
      xValues[i] = coefficients[i * 2];
      yValues[i] = coefficients[i * 2 + 1];
    }

    return () -> new IntUnaryOperator() {
      private final UnivariateFunction f = interpolator.interpolate(xValues, yValues);

      @Override
      public int applyAsInt(int x) {
        return (int) Math.round(f.value(Math.min(Math.max(x, xValues[0]), xValues[xValues.length - 1])));
      }
    };
  }

  public static Provider<IntUnaryOperator> interpolate(@Nonnull Coefficients coefficients) {
    double[] xAndY = coefficients.get();
    if ((xAndY.length & 1) == 1) {
      throw new IllegalArgumentException(String.format("Number %d of coefficients %s is not even", xAndY.length, coefficients.name()));
    }

    for (Interpolators i : Interpolators.values()) {
      if (xAndY.length / 2 >= i.minPoints) {
        return i.interpolate(xAndY);
      }
    }
    throw new IllegalArgumentException(String.format("Number of points %d from %s is too small", xAndY.length / 2, coefficients.name()));
  }
}
