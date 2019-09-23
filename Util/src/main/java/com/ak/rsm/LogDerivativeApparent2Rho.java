package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

final class LogDerivativeApparent2Rho extends AbstractLogApparent2Rho {
  LogDerivativeApparent2Rho(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  @Override
  double innerValue(double sums) {
    return log(Math.abs(sums)) * Math.signum(sums);
  }

  @Override
  double commonFactor(double k, int n) {
    return pow(k, n) * n * n;
  }

  @Override
  DoubleBinaryOperator sum(@Nonnegative double h) {
    return (sign, n) -> 1.0 / pow(hypot(radius(sign), 2.0 * n * h), 3.0);
  }
}
