package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

abstract class AbstractDerivativeApparent extends AbstractApparent {
  @Nonnegative
  private final double lCC;

  AbstractDerivativeApparent(@Nonnull TetrapolarSystem system) {
    super(system);
    lCC = system.getL();
  }

  final double getL() {
    return lCC;
  }

  @Override
  final DoubleBinaryOperator sum(@Nonnegative double hToL) {
    return (sign, n) -> hToL / pow(hypot(factor(sign), 4.0 * n * hToL), 3.0);
  }

  @Override
  public final int sumFactor(@Nonnegative int n) {
    return n * n;
  }
}
