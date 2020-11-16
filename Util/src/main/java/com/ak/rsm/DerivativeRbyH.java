package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

final class DerivativeRbyH extends AbstractResistanceSumValue {
  DerivativeRbyH(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    return -8.0 * sums;
  }

  @Override
  DoubleBinaryOperator sum(double h) {
    return (sign, n) -> h / pow(hypot(radius(sign), 2.0 * n * h), 3.0);
  }

  @Override
  public int sumFactor(int n) {
    return n * n;
  }
}
