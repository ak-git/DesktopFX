package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

final class LogDerivativeApparent2Rho extends AbstractLogApparent2Rho {
  LogDerivativeApparent2Rho(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  @Override
  double innerValue(double sums) {
    return StrictMath.log(Math.abs(-4.0 * electrodesFactor() * sums));
  }

  @Override
  int commonFactor(int n) {
    return n * n;
  }

  @Override
  DoubleBinaryOperator sum(@Nonnegative double h) {
    return (sign, n) -> h / pow(hypot(radius(sign), 2.0 * n * h), 3.0);
  }
}
