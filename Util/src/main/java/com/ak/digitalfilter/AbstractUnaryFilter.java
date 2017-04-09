package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

abstract class AbstractUnaryFilter extends AbstractDigitalFilter {
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

  abstract void publishUnary(int in);
}