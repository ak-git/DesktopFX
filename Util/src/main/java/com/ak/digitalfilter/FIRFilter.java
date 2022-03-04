package com.ak.digitalfilter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class FIRFilter extends AbstractBufferFilter {
  @Nonnull
  private final double[] coefficients;

  FIRFilter(@Nonnull double[] coefficients) {
    super(coefficients.length);
    this.coefficients = coefficients.clone();
  }

  @Override
  int apply(@Nonnegative int nowIndex) {
    double result = 0;
    for (var i = 0; i < coefficients.length; i++) {
      result += get(1 + i + nowIndex) * coefficients[i];
    }
    return (int) Math.round(result);
  }
}