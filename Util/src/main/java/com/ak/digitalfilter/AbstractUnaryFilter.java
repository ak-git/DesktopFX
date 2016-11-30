package com.ak.digitalfilter;

import java.util.Arrays;

import javax.annotation.Nonnegative;

abstract class AbstractUnaryFilter extends AbstractDigitalFilter {
  @Override
  public final void accept(int... in) {
    if (in.length == 1) {
      publishUnary(in[0]);
    }
    else {
      throw new IllegalArgumentException(String.format("%s %s", toString(), Arrays.toString(in)));
    }
  }

  @Nonnegative
  @Override
  public final int size() {
    return 1;
  }

  abstract void publishUnary(int in);
}