package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.BivariateFunction;

abstract class AbstractLogApparent2Rho implements BivariateFunction {
  @Nonnull
  private final TetrapolarSystem electrodeSystem;

  AbstractLogApparent2Rho(@Nonnull TetrapolarSystem electrodeSystem) {
    this.electrodeSystem = electrodeSystem;
  }

  @Override
  public final double value(double k, @Nonnegative double Lh) {
    double sL = electrodeSystem.sToL();
    DoubleBinaryOperator sumMns = sum(k, -1);
    DoubleBinaryOperator sumPls = sum(k, 1);
    return value(Lh, sL, sumMns.applyAsDouble(Lh, sL) - sumPls.applyAsDouble(Lh, sL));
  }

  abstract double value(double Lh, double sL, double sums);

  abstract DoubleBinaryOperator sum(double k, int sign);
}
