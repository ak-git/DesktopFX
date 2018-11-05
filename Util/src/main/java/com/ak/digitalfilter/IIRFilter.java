package com.ak.digitalfilter;

import javax.annotation.Nonnull;

import com.ak.numbers.RangeUtils;

final class IIRFilter extends AbstractOperableFilter {
  @Nonnull
  private final FIRFilter filter;
  private int sum;

  IIRFilter(@Nonnull double[] coefficients) {
    filter = new FIRFilter(RangeUtils.reverseOrder(coefficients));
  }

  @Override
  public int applyAsInt(int in) {
    int result = sum + in;
    sum = filter.applyAsInt(result);
    return result;
  }

  @Override
  public double getDelay() {
    return 0.0;
  }
}