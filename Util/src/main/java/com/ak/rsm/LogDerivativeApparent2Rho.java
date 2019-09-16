package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

final class LogDerivativeApparent2Rho extends AbstractLogApparent2Rho {
  LogDerivativeApparent2Rho(@Nonnegative double sToL, @Nonnegative double Lh) {
    super(sToL, Lh);
  }

  @Override
  double innerValue(double sums) {
    return log(Math.abs(sums));
  }

  @Override
  double commonFactor(double k, int n) {
    return pow(k, n) * n * n;
  }

  @Override
  DoubleBinaryOperator sum() {
    return (sign, n) -> 1.0 / pow(hypot(Lh() * (1.0 + sign * sToL()), 4.0 * n), 3.0);
  }
}
