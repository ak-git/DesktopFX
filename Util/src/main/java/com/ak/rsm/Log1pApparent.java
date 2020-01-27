package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log1p;

final class Log1pApparent extends AbstractApparent {
  Log1pApparent(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    return log1p(electrodesFactor() * sums);
  }

  @Override
  DoubleBinaryOperator sum(double h) {
    return (sign, n) -> 1.0 / hypot(radius(sign), 2.0 * n * h);
  }

  @Override
  public int sumFactor(int n) {
    return 1;
  }
}
