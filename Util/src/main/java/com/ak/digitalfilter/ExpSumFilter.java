package com.ak.digitalfilter;

final class ExpSumFilter extends AbstractOperableFilter {
  private int y;
  private boolean resetFlag = true;

  @Override
  public int applyAsInt(int in) {
    if (resetFlag) {
      y = in;
      resetFlag = false;
    }
    int diff = in - y;
    y += (diff >> 5) - (diff >> 6);
    return y;
  }
}