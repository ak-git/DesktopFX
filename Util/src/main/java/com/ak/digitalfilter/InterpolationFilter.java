package com.ak.digitalfilter;

final class InterpolationFilter extends AbstractRateConversionFilter {
  InterpolationFilter(int interpolateFactor) {
    super(interpolateFactor);
  }

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