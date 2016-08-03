package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnegative;

abstract class OperableFilter extends AbstractDigitalFilter implements IntUnaryOperator {
  @Override
  public final void accept(int... in) {
    if (in.length == 1) {
      publish(applyAsInt(in[0]));
    }
    else {
      throw new IllegalArgumentException(Arrays.toString(in));
    }
  }

  @Nonnegative
  @Override
  public final int size() {
    return 1;
  }
}
