package com.ak.rsm;

import java.util.function.IntToDoubleFunction;

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
    IntToDoubleFunction sum = sum(k, Lh, sL);
    return value(Lh, sL, sum.applyAsDouble(-1) - sum.applyAsDouble(1));
  }

  abstract double value(double Lh, double sL, double sums);

  abstract IntToDoubleFunction sum(double k, double Lh, double sL);
}
