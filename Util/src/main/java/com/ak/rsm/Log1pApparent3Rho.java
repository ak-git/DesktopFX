package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log1p;

final class Log1pApparent3Rho extends AbstractLogApparent3Rho {
  Log1pApparent3Rho(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  @Override
  double innerValue(double sums) {
    return log1p(electrodesFactor() * sums);
  }

  @Override
  DoubleBinaryOperator sum(@Nonnull double[] q, @Nonnegative double h) {
    return (sign, n) -> 1.0 / hypot(radius(sign), 2.0 * n * h);
  }
}
