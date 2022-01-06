package com.ak.rsm.apparent;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.system.RelativeTetrapolarSystem;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

final class SecondDerivativeApparentByPhiK extends AbstractResistanceSumValue {
  SecondDerivativeApparentByPhiK(@Nonnull RelativeTetrapolarSystem system) {
    super(system);
  }

  @Override
  double multiply(double sums) {
    return -16.0 * electrodesFactor() * sums;
  }

  @Override
  DoubleBinaryOperator sum(@Nonnegative double hToL) {
    return (sign, n) -> hToL / pow(hypot(factor(sign), 4.0 * n * hToL), 3.0);
  }

  @Override
  public int sumFactor(@Nonnegative int n) {
    return n * n * n;
  }
}
