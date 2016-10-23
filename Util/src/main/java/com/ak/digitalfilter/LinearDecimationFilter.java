package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class LinearDecimationFilter extends AbstractLinearRateConversionFilter {
  private int counter;

  LinearDecimationFilter(@Nonnegative int decimateFactor) {
    super(decimateFactor);
  }

  @Override
  public double getDelay(double beforeDelay) {
    return (beforeDelay - (factor - 1) / 2.0) / factor;
  }

  @Nonnegative
  @Override
  public double getFrequencyFactor() {
    return 1.0 / factor;
  }

  @Override
  void publishUnary(int in) {
    counter = (++counter) % factor;
    int reduced = integrator.applyAsInt(in);
    if (counter == 0) {
      publish(comb.applyAsInt(reduced) / factor);
    }
  }
}