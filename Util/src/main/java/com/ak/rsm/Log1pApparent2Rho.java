package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log1p;
import static java.lang.StrictMath.pow;

final class Log1pApparent2Rho extends AbstractLogApparent2Rho {
  Log1pApparent2Rho(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  @Override
  double innerValue(double sums) {
    return log1p(electrodesFactor() * sums);
  }

  @Override
  double commonFactor(double k, @Nonnegative int n) {
    return pow(k, n);
  }

  @Override
  DoubleBinaryOperator sum(@Nonnegative double h) {
    return (sign, n) -> 1.0 / hypot(radius(sign), 2.0 * n * h);
  }
}
