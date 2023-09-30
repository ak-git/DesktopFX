package com.ak.digitalfilter;

final class AutoZeroFilter extends AbstractOperableFilter {
  private int y;

  @Override
  public int applyAsInt(int in) {
    if (checkResetAndClear()) {
      y = in;
    }
    return in - y;
  }
}