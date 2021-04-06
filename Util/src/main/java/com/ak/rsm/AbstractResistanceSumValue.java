package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;
import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractResistanceSumValue implements ResistanceSumValue {
  @Nonnull
  private final RelativeTetrapolarSystem system;

  AbstractResistanceSumValue(@Nonnull RelativeTetrapolarSystem system) {
    this.system = system;
  }

  final double factor(double sign) {
    return system.factor(sign);
  }

  @Override
  public final double value(@Nonnegative double hToL, @Nonnull IntToDoubleFunction qn) {
    DoubleBinaryOperator sum = sum(hToL);
    return multiply(Layers.sum(n -> qn.applyAsDouble(n) * (sum.applyAsDouble(-1.0, n) - sum.applyAsDouble(1.0, n))));
  }

  abstract double multiply(double sums);

  abstract DoubleBinaryOperator sum(@Nonnegative double hToL);
}
