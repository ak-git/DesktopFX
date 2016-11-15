package com.ak.digitalfilter;

final class IntegrateFilter extends AbstractOperableFilter {
  private int sum;

  @Override
  public int applyAsInt(int in) {
    sum += in;
    return sum;
  }
}