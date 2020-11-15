package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

abstract class AbstractRateConversionFilter extends AbstractUnaryFilter {
  final int factor;

  AbstractRateConversionFilter(@Nonnegative int factor) {
    if (factor > 0) {
      this.factor = factor;
    }
    else {
      throw new IllegalArgumentException("factor must be > 0, but found %d".formatted(factor));
    }
  }
}