package com.ak.digitalfilter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class LinearInterpolationFilter extends AbstractLinearRateConversionFilter {
  @Nonnull
  private final InterpolationFilter interpolationFilter;

  LinearInterpolationFilter(@Nonnegative int interpolateFactor) {
    interpolationFilter = new InterpolationFilter(interpolateFactor, expand -> integrator.applyAsInt(expand) / interpolateFactor);
    interpolationFilter.forEach(this::publish);
  }

  @Override
  public double getDelay(double beforeDelay) {
    return interpolationFilter.getDelay(beforeDelay);
  }

  @Nonnegative
  @Override
  public double getFrequencyFactor() {
    return interpolationFilter.getFrequencyFactor();
  }

  @Override
  void publishUnary(int in) {
    interpolationFilter.publishUnary(comb.applyAsInt(in));
  }
}