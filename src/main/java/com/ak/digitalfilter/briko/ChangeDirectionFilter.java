package com.ak.digitalfilter.briko;

import com.ak.digitalfilter.AbstractOperableFilter;

import javax.annotation.Nonnegative;

public final class ChangeDirectionFilter extends AbstractOperableFilter {
  @Nonnegative
  private final int stableInterval;
  private int counts;
  private int prev;

  public ChangeDirectionFilter(@Nonnegative int stableInterval) {
    this.stableInterval = stableInterval;
  }

  @Override
  public int applyAsInt(int in) {
    if (checkResetAndClear()) {
      counts = 0;
      prev = in;
    }

    boolean signChanged = false;
    if (in == prev) {
      counts++;
    }
    else {
      if (counts > stableInterval) {
        signChanged = true;
      }
      counts = 0;
    }
    prev = in;
    return signChanged ? 1 : 0;
  }
}