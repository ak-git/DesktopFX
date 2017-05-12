package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class DecimationFilter extends AbstractRateConversionFilter {
  private int counter;

  DecimationFilter(@Nonnegative int decimateFactor) {
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
    if (counter == 0) {
      publish(in);
    }
  }
}