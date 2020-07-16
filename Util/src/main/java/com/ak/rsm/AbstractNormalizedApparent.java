package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;

abstract class AbstractNormalizedApparent extends AbstractApparent {
  AbstractNormalizedApparent(@Nonnull TetrapolarSystem system) {
    super(system);
  }

  @Override
  final DoubleBinaryOperator sum(double h) {
    return (sign, n) -> 1.0 / hypot(radius(sign), 2.0 * n * h);
  }

  @Override
  public final int sumFactor(int n) {
    return 1;
  }
}
