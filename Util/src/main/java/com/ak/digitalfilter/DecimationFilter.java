package com.ak.digitalfilter;

final class DecimationFilter extends AbstractRateConversionFilter {
  private int counter;

  DecimationFilter(int decimateFactor) {
    super(decimateFactor);
  }

  @Override
  public double getDelay() {
    return (-(factor - 1) / 2.0) / factor;
  }

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