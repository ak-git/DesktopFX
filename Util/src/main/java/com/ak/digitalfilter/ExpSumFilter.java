package com.ak.digitalfilter;

final class ExpSumFilter extends AbstractOperableFilter {
  private int y;

  @Override
  public int applyAsInt(int in) {
    if (checkResetAndClear()) {
      y = in;
    }
    int diff = in - y;
    y += (diff >> 5) - (diff >> 6);
    return y;
  }
}