package com.ak.digitalfilter;

import java.util.function.IntUnaryOperator;

public abstract class AbstractOperableFilter extends AbstractUnaryFilter implements IntUnaryOperator {
  @Override
  final void publishUnary(int in) {
    publish(applyAsInt(in));
  }
}
