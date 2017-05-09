package com.ak.digitalfilter;

import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class DecimationFilter extends AbstractRateConversionFilter {
  @Nonnull
  private final IntUnaryOperator decimateOperator;
  private int counter;

  DecimationFilter(@Nonnegative int decimateFactor, @Nonnull IntUnaryOperator decimateOperator) {
    super(decimateFactor);
    this.decimateOperator = decimateOperator;
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
      publish(decimateOperator.applyAsInt(in));
    }
  }
}