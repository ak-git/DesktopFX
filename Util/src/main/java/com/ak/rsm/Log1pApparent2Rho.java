package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log1p;
import static java.lang.StrictMath.pow;

final class Log1pApparent2Rho extends AbstractLogApparent2Rho {
  Log1pApparent2Rho(@Nonnegative double sToL) {
    super(sToL);
  }

  @Override
  double innerValue(@Nonnegative double Lh, double sums) {
    return log1p(electrodesFactor(Lh) * sums);
  }

  @Override
  double commonFactor(double k, @Nonnegative int n) {
    return pow(k, n);
  }

  @Override
  DoubleBinaryOperator sum(@Nonnegative double Lh) {
    return (sign, n) -> 1.0 / hypot(Lh * (1.0 + sign * sToL()), 4.0 * n);
  }
}
