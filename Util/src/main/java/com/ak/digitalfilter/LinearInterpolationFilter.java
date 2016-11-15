package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class LinearInterpolationFilter extends AbstractLinearRateConversionFilter {
  LinearInterpolationFilter(@Nonnegative int interpolateFactor) {
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
    int hold = comb.applyAsInt(in);
    for (int i = 0; i < factor; i++) {
      publish(integrator.applyAsInt(hold) / factor);
    }
  }
}