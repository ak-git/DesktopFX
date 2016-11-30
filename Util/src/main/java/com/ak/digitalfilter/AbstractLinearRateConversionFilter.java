package com.ak.digitalfilter;

import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnegative;

abstract class AbstractLinearRateConversionFilter extends AbstractUnaryFilter {
  final IntUnaryOperator comb = new CombFilter(1);
  final IntUnaryOperator integrator = new IntegrateFilter();
  final int factor;

  AbstractLinearRateConversionFilter(@Nonnegative int factor) {
    if (factor > 0) {
      this.factor = factor;
    }
    else {
      throw new IllegalArgumentException(String.format("factor must be > 0, but found %d", factor));
    }
  }

  @Override
  public final double getDelay() {
    return getDelay(0.0);
  }
}