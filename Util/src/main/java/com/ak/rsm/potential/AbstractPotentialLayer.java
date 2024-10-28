package com.ak.rsm.potential;

import javax.annotation.Nonnegative;
import java.util.function.DoubleUnaryOperator;

public abstract class AbstractPotentialLayer {
  @Nonnegative
  private final double r;

  AbstractPotentialLayer(@Nonnegative double r) {
    this.r = Math.abs(r);
  }

  final double value(DoubleUnaryOperator functionR) {
    return functionR.applyAsDouble(r);
  }
}
