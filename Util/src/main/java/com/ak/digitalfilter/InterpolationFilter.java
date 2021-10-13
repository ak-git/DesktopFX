package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class InterpolationFilter extends AbstractRateConversionFilter {
  InterpolationFilter(@Nonnegative int interpolateFactor) {
    super(interpolateFactor);
  }

  @Nonnegative
  @Override
  public double getFrequencyFactor() {
    return factor;
  }

  @Override
  void publishUnary(int in) {
    for (var i = 0; i < factor; i++) {
      publish(in);
    }
  }
}