package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

class Apparent2Rho extends AbstractApparentRho implements DoubleBinaryOperator {
  private Apparent2Rho(@Nonnull ResistanceSumValue apparent) {
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
    return pow(k, n) * sumFactor(n);
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

  static DoubleBinaryOperator newDerivativeApparentByK2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new DerivativeApparentByK(system)) {
      @Override
      double kFactor(double k, @Nonnegative int n) {
        return pow(k, n - 1.0) * sumFactor(n);
      }
    };
  }

  static DoubleBinaryOperator newSecondDerivativeApparentByPhiK2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new SecondDerivativeApparentByPhiK(system)) {
      @Override
      double kFactor(double k, @Nonnegative int n) {
        return pow(k, n - 1.0) * sumFactor(n);
      }
    };
  }

  static DoubleBinaryOperator newSecondDerivativeApparentByPhiPhi2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new DoubleBinaryOperator() {
      @Nonnull
      private final DoubleBinaryOperator apparentByPhi2Rho = newDerivativeApparentByPhi2Rho(system);
      @Nonnull
      private final DoubleBinaryOperator secondPart = new Apparent2Rho(new AbstractResistanceSumValue(system) {
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

      @Override
      public double applyAsDouble(double k, @Nonnegative double hToL) {
        return apparentByPhi2Rho.applyAsDouble(k, hToL) / hToL + secondPart.applyAsDouble(k, hToL);
      }
    };
  }
}
