package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class InterpolationFilter extends AbstractRateConversionFilter {
  InterpolationFilter(@Nonnegative int interpolateFactor) {
    super(interpolateFactor);
  }

  @Override
  public double getDelay(double beforeDelay) {
    return beforeDelay * factor + (factor - 1) / 2.0;
  }

  @Nonnegative
  @Override
  public double getFrequencyFactor() {
    return factor;
  }

  @Override
  void publishUnary(int in) {
    for (int i = 0; i < factor; i++) {
      publish(in);
    }
  }
}