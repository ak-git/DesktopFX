package com.ak.rsm.potential;

import java.util.function.DoubleUnaryOperator;

public abstract class AbstractPotentialLayer {
  private final double r;

  AbstractPotentialLayer(double r) {
    this.r = Math.abs(r);
  }

  final double value(DoubleUnaryOperator functionR) {
    return functionR.applyAsDouble(r);
  }
}
