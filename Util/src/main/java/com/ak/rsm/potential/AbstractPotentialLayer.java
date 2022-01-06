package com.ak.rsm.potential;

import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public abstract class AbstractPotentialLayer {
  @Nonnegative
  private final double r;

  AbstractPotentialLayer(@Nonnegative double r) {
    this.r = Math.abs(r);
  }

  final double value(@Nonnull DoubleUnaryOperator functionR) {
    return functionR.applyAsDouble(r);
  }
}
