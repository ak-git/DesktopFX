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

  public final double value(@Nonnegative double rho, @Nonnull DoubleUnaryOperator functionR) {
    return (rho / (2 * Math.PI)) * functionR.applyAsDouble(r);
  }
}
