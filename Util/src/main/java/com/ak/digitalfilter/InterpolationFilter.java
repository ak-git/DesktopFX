package com.ak.digitalfilter;

import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class InterpolationFilter extends AbstractRateConversionFilter {
  @Nonnull
  private final IntUnaryOperator interpolateOperator;

  InterpolationFilter(@Nonnegative int interpolateFactor, @Nonnull IntUnaryOperator interpolateOperator) {
    super(interpolateFactor);
    this.interpolateOperator = interpolateOperator;
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
      publish(interpolateOperator.applyAsInt(in));
    }
  }
}