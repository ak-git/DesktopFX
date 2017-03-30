package com.ak.numbers;

import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;

public enum Interpolators {
  ;

  public static Provider<IntUnaryOperator> interpolate(@Nonnull Coefficients coefficients) {
    double[] all = coefficients.get();
    if ((all.length & 1) == 1) {
      throw new IllegalArgumentException(String.format("Number %d of coefficients %s is not even", all.length, coefficients.name()));
    }
    double[] xValues = new double[all.length / 2];
    double[] yValues = new double[all.length / 2];

    for (int i = 0; i < xValues.length; i++) {
      xValues[i] = all[i * 2];
      yValues[i] = all[i * 2 + 1];
    }

    return () -> new IntUnaryOperator() {
      private final UnivariateFunction interpolator = new AkimaSplineInterpolator().interpolate(xValues, yValues);

      @Override
      public int applyAsInt(int x) {
        return (int) Math.round(interpolator.value(Math.min(Math.max(x, xValues[0]), xValues[xValues.length - 1])));
      }
    };
  }
}
