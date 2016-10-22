package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnegative;

abstract class AbstractOperableFilter extends AbstractDigitalFilter implements IntUnaryOperator {
  @Override
  public final void accept(int... in) {
    if (in.length == 1) {
      publish(applyAsInt(in[0]));
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
}
