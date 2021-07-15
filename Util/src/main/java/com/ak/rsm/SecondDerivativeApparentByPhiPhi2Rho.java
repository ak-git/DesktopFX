package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

final class SecondDerivativeApparentByPhiPhi2Rho implements DoubleBinaryOperator {
  private final DoubleBinaryOperator apparentByPhi2Rho;
  private final DoubleBinaryOperator secondPart;

  SecondDerivativeApparentByPhiPhi2Rho(@Nonnull RelativeTetrapolarSystem system) {
    apparentByPhi2Rho = Apparent2Rho.newDerivativeApparentByPhi2Rho(system);
    secondPart = new Apparent2Rho(new AbstractResistanceSumValue(system) {
      @Override
      double multiply(double sums) {
        return 16.0 * electrodesFactor() * sums * 48.0;
      }

      @Override
      DoubleBinaryOperator sum(@Nonnegative double hToL) {
        return (sign, n) -> (hToL * hToL) / pow(hypot(factor(sign), 4.0 * n * hToL), 5.0);
      }

      @Override
      public int sumFactor(@Nonnegative int n) {
        return n * n * n * n;
      }
    });
  }

  @Override
  public double applyAsDouble(double k, @Nonnegative double hToL) {
    return apparentByPhi2Rho.applyAsDouble(k, hToL) / hToL + secondPart.applyAsDouble(k, hToL);
  }
}
