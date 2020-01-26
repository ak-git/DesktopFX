package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

final class LogDerivativeApparent extends AbstractApparent {
  LogDerivativeApparent(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    double a = -4.0 * electrodesFactor() * sums;
    return Math.signum(a) * StrictMath.log(Math.abs(a));
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
