package com.ak.digitalfilter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class LinearDecimationFilter extends AbstractLinearRateConversionFilter {
  @Nonnull
  private final DecimationFilter decimationFilter;

  LinearDecimationFilter(@Nonnegative int decimateFactor) {
    decimationFilter = new DecimationFilter(decimateFactor, reduced -> comb.applyAsInt(reduced) / decimateFactor);
    decimationFilter.forEach(this::publish);
  }

  @Override
  public double getDelay(double beforeDelay) {
    return decimationFilter.getDelay(beforeDelay);
  }

  @Nonnegative
  @Override
  public double getFrequencyFactor() {
    return decimationFilter.getFrequencyFactor();
  }

  @Override
  void publishUnary(int in) {
    decimationFilter.publishUnary(integrator.applyAsInt(in));
  }
}