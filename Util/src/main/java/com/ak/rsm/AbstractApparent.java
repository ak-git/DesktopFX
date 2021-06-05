package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;

abstract class AbstractApparent extends AbstractResistanceSumValue {
  AbstractApparent(@Nonnull RelativeTetrapolarSystem system) {
    super(system);
  }

  final double electrodesFactor() {
    return 2.0 / (1.0 / factor(-1) - 1.0 / factor(1));
  }

  @Override
  DoubleBinaryOperator sum(double hToL) {
    return (sign, n) -> 1.0 / hypot(factor(sign), 4.0 * n * hToL);
  }
}
