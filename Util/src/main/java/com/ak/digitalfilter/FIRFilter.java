package com.ak.digitalfilter;

final class FIRFilter extends AbstractBufferFilter {
  private final double[] coefficients;

  FIRFilter(double[] coefficients) {
    super(coefficients.length);
    this.coefficients = coefficients.clone();
  }

  @Override
  int apply(int nowIndex) {
    double result = 0;
    for (var i = 0; i < coefficients.length; i++) {
      result += get(1 + i + nowIndex) * coefficients[i];
    }
    return (int) Math.round(result);
  }
}