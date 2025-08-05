package com.ak.digitalfilter;

abstract class AbstractRateConversionFilter extends AbstractUnaryFilter {
  final int factor;

  AbstractRateConversionFilter(int factor) {
    if (factor > 0) {
      this.factor = factor;
    }
    else {
      throw new IllegalArgumentException("factor must be > 0, but found %d".formatted(factor));
    }
  }
}