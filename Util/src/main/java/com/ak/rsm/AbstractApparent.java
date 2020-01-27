package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;
import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractApparent implements Apparent {
  @Nonnull
  private final TetrapolarSystem system;

  AbstractApparent(@Nonnull TetrapolarSystem system) {
    this.system = system;
  }

  final double electrodesFactor() {
    return 2.0 / (1.0 / Math.abs(radius(-1.0)) - 1.0 / radius(1.0));
  }

  final double radius(double sign) {
    if (sign < 0) {
      return system.radiusMns();
    }
    else {
      return system.radiusPls();
    }
  }

  @Override
  public final double value(@Nonnegative double h, @Nonnull IntToDoubleFunction qn) {
    DoubleBinaryOperator sum = sum(h);
    return multiply(Layers.sum(n -> qn.applyAsDouble(n) * (sum.applyAsDouble(-1.0, n) - sum.applyAsDouble(1.0, n))));
  }

  abstract double multiply(double sums);

  abstract DoubleBinaryOperator sum(@Nonnegative double h);
}
