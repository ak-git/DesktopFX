package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

class Apparent2Rho extends AbstractApparentRho implements DoubleBinaryOperator {
  Apparent2Rho(@Nonnull ResistanceSumValue apparent) {
    super(apparent);
  }

  @Override
  public final double applyAsDouble(double k, @Nonnegative double hToL) {
    if (Double.compare(k, 0.0) == 0 || Double.compare(hToL, 0.0) == 0) {
      return value(hToL, value -> 0.0);
    }
    else {
      return value(hToL, n -> kFactor(k, n));
    }
  }

  double kFactor(double k, @Nonnegative int n) {
    return StrictMath.pow(k, n) * sumFactor(n);
  }

  static DoubleBinaryOperator newLog1pApparent2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new Log1pApparent(system));
  }

  /**
   * Calculates Apparent Resistance divided by Rho1
   */
  static DoubleBinaryOperator newNormalizedApparent2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new NormalizedApparent(system));
  }

  static DoubleBinaryOperator newDerivativeApparentByPhi2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new DerivativeApparentByPhi(system));
  }
}
