package com.ak.digitalfilter;

import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnegative;

final class LinearDecimationFilter extends AbstractUnaryFilter {
  private final IntUnaryOperator integrator = new IntegrateFilter();
  private final IntUnaryOperator comb = new CombFilter(1);
  private final int decimateFactor;
  private int counter;

  LinearDecimationFilter(@Nonnegative int decimateFactor) {
    if (decimateFactor > 0) {
      this.decimateFactor = decimateFactor;
    }
    else {
      throw new IllegalArgumentException(String.format("decimateFactor must be > 0, but found %d", decimateFactor));
    }
  }

  @Override
  public double getDelay() {
    return getDelay(0.0);
  }

  @Override
  public double getDelay(double beforeDelay) {
    return (beforeDelay - (decimateFactor - 1) / 2.0) / decimateFactor;
  }

  @Override
  public String toString() {
    return String.format("Decimator /= %d (delay %.1f)", decimateFactor, getDelay());
  }

  @Override
  void publishUnary(int in) {
    counter = (++counter) % decimateFactor;
    int reduced = integrator.applyAsInt(in);
    if (counter == 0) {
      publish(comb.applyAsInt(reduced) / decimateFactor);
    }
  }
}
