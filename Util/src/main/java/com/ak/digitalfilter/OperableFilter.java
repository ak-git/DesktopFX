package com.ak.digitalfilter;

import java.util.function.IntUnaryOperator;

abstract class OperableFilter extends AbstractDigitalFilter implements IntUnaryOperator {
  @Override
  public final void accept(int in) {
    publish(applyAsInt(in));
  }
}
