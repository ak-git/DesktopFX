package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.BivariateFunction;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.log1p;
import static java.lang.StrictMath.pow;

final class Log1pRhoApparent2 implements BivariateFunction {
  @Nonnull
  private final TetrapolarSystem electrodeSystem;

  Log1pRhoApparent2(@Nonnull TetrapolarSystem electrodeSystem) {
    this.electrodeSystem = electrodeSystem;
  }

  @Override
  public double value(double k, @Nonnegative double Lh) {
    double sL = electrodeSystem.sToL();
    DoubleBinaryOperator sumMns = sum(k, -1);
    DoubleBinaryOperator sumPls = sum(k, 1);
    return log1p((Lh * (1.0 - pow(sL, 2.0)) / sL) * (sumMns.applyAsDouble(Lh, sL) - sumPls.applyAsDouble(Lh, sL)));
  }

  private static DoubleBinaryOperator sum(double k, int sign) {
    return (Lh, sL) -> Layers.sum(n -> pow(k, n), n -> hypot(Lh * (1.0 + sign * sL), 4.0 * n));
  }
}
