package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

abstract class AbstractUnaryFilter extends AbstractDigitalFilter {
  private boolean resetFlag = true;

  @Override
  public final void accept(int... in) {
    if (in.length == 1) {
      publishUnary(in[0]);
    }
    else {
      illegalArgumentException(in);
    }
  }

  @Nonnegative
  @Override
  public final int getOutputDataSize() {
    return 1;
  }

  @Override
  public final void reset() {
    resetFlag = true;
  }

  abstract void publishUnary(int in);

  final boolean checkResetAndClear() {
    if (resetFlag) {
      resetFlag = false;
      return true;
    }
    else {
      return false;
    }
  }
}