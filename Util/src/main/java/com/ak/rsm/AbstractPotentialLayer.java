package com.ak.rsm;

import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractPotentialLayer {
  @Nonnegative
  private final double r;

  AbstractPotentialLayer(double r) {
    this.r = Math.abs(r);
  }

  final double value(@Nonnull DoubleUnaryOperator functionR) {
    return functionR.applyAsDouble(r);
  }
}
