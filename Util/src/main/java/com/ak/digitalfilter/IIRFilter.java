package com.ak.digitalfilter;

import com.ak.numbers.RangeUtils;

final class IIRFilter extends AbstractOperableFilter {
  private final FIRFilter filter;
  private int sum;

  IIRFilter(double[] coefficients) {
    filter = new FIRFilter(RangeUtils.reverseOrder(coefficients));
  }

  @Override
  public int applyAsInt(int in) {
    int result = sum + in;
    sum = filter.applyAsInt(result);
    return result;
  }
}