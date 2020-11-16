package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;
import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractResistanceSumValue implements ResistanceSumValue {
  @Nonnull
  private final TetrapolarSystem system;

  AbstractResistanceSumValue(@Nonnull TetrapolarSystem system) {
    this.system = system;
  }

  final double radius(double sign) {
    return system.radius(sign);
  }

  @Override
  public final double value(@Nonnegative double h, @Nonnull IntToDoubleFunction qn) {
    DoubleBinaryOperator sum = sum(h);
    return multiply(Layers.sum(n -> qn.applyAsDouble(n) * (sum.applyAsDouble(-1.0, n) - sum.applyAsDouble(1.0, n))));
  }

  abstract double multiply(double sums);

  abstract DoubleBinaryOperator sum(@Nonnegative double h);
}
