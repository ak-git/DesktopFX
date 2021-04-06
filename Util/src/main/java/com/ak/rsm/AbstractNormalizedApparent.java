package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;

abstract class AbstractNormalizedApparent extends AbstractApparent {
  AbstractNormalizedApparent(@Nonnull RelativeTetrapolarSystem system) {
    super(system);
  }

  @Override
  final DoubleBinaryOperator sum(double hToL) {
    return (sign, n) -> 1.0 / hypot(factor(sign), 4.0 * n * hToL);
  }

  @Override
  public final int sumFactor(int n) {
    return 1;
  }
}
