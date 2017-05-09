package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

abstract class AbstractRateConversionFilter extends AbstractUnaryFilter {
  final int factor;

  AbstractRateConversionFilter(@Nonnegative int factor) {
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