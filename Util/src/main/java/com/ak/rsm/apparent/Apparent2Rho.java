package com.ak.rsm.apparent;

import java.util.function.DoubleBinaryOperator;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.medium.RelativeMediumLayers;
import com.ak.rsm.system.RelativeTetrapolarSystem;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

public class Apparent2Rho extends AbstractApparentRho implements ToDoubleFunction<RelativeMediumLayers> {
  private Apparent2Rho(@Nonnull ResistanceSumValue apparent) {
    super(apparent);
  }

  @Override
  public final double applyAsDouble(@Nonnull RelativeMediumLayers kw) {
    if (Double.compare(kw.k12(), 0.0) == 0 || Double.compare(kw.hToL(), 0.0) == 0) {
      return value(kw.hToL(), value -> 0.0);
    }
    else {
      return value(kw.hToL(), n -> kFactor(kw.k12(), n));
    }
  }

  double kFactor(double k, @Nonnegative int n) {
    return pow(k, n) * sumFactor(n);
  }

  static ToDoubleFunction<RelativeMediumLayers> newLog1pApparent2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new Log1pApparent(system));
  }

  /**
   * Calculates Apparent Resistance divided by Rho1
   */
  public static ToDoubleFunction<RelativeMediumLayers> newNormalizedApparent2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new NormalizedApparent(system));
  }

  static ToDoubleFunction<RelativeMediumLayers> newDerivativeApparentByPhi2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new DerivativeApparentByPhi(system));
  }

  static ToDoubleFunction<RelativeMediumLayers> newDerivativeApparentByK2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new DerivativeApparentByK(system)) {
      @Override
      double kFactor(double k, @Nonnegative int n) {
        return pow(k, n - 1.0) * sumFactor(n);
      }
    };
  }

  static ToDoubleFunction<RelativeMediumLayers> newSecondDerivativeApparentByPhiK2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new SecondDerivativeApparentByPhiK(system)) {
      @Override
      double kFactor(double k, @Nonnegative int n) {
        return pow(k, n - 1.0) * sumFactor(n);
      }
    };
  }

  static ToDoubleFunction<RelativeMediumLayers> newSecondDerivativeApparentByPhiPhi2Rho(@Nonnull RelativeTetrapolarSystem system) {
    return new ToDoubleFunction<>() {
      @Nonnull
      private final ToDoubleFunction<RelativeMediumLayers> apparentByPhi2Rho = newDerivativeApparentByPhi2Rho(system);
      @Nonnull
      private final ToDoubleFunction<RelativeMediumLayers> secondPart = new Apparent2Rho(new AbstractResistanceSumValue(system) {
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
      public double applyAsDouble(@Nonnull RelativeMediumLayers kw) {
        return apparentByPhi2Rho.applyAsDouble(kw) / kw.hToL() + secondPart.applyAsDouble(kw);
      }
    };
  }
}
