package com.ak.digitalfilter;

final class IntegrateFilter extends AbstractOperableFilter {
  private int sum;

  @Override
  public int applyAsInt(int in) {
    sum += in;
    return sum;
  }

  @Override
  public double getDelay() {
    return -0.5;
  }
}