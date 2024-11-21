package com.ak.rsm.apparent;

import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.DeltaH;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import java.util.function.DoubleBinaryOperator;
import java.util.function.ToDoubleFunction;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

public class Apparent2Rho extends AbstractApparentRho implements ToDoubleFunction<RelativeMediumLayers> {
  private Apparent2Rho(ResistanceSumValue apparent) {
    super(apparent);
  }

  @Override
  public final double applyAsDouble(RelativeMediumLayers kw) {
    if (kw.size() == 1) {
      return value(0.0, _ -> 0.0);
    }
    else {
      return value(kw.hToL().value(), n -> kFactor(kw.k().value(), n));
    }
  }

  double kFactor(double k, @Nonnegative int n) {
    return pow(k, n) * sumFactor(n);
  }

  /**
   * Calculates Apparent Resistance divided by Rho1
   */
  public static ToDoubleFunction<RelativeMediumLayers> newApparentDivRho1(RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new NormalizedApparent(system));
  }

  public static ToDoubleFunction<RelativeMediumLayers> newDerApparentByPhiDivRho1(TetrapolarSystem system, double dh) {
    if (Double.isNaN(dh)) {
      return newDerApparentByPhiDivRho1(system.relativeSystem());
    }
    return kw -> {
      double rho1 = 1.0;
      double rho2 = rho1 / Layers.getRho1ToRho2(kw.k().value());
      return TetrapolarDerivativeResistance.of(system).dh(DeltaH.H1.apply(dh))
          .rho1(rho1).rho2(rho2).h(kw.hToL().value() * system.lCC()).derivativeResistivity();
    };
  }

  public static ToDoubleFunction<RelativeMediumLayers> newDerApparentByPhiDivRho1(RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new DerivativeApparentByPhi(system));
  }

  public static ToDoubleFunction<RelativeMediumLayers> newDerApparentByKDivRho1(RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new DerivativeApparentByK(system)) {
      @Override
      double kFactor(double k, @Nonnegative int n) {
        return pow(k, n - 1.0) * sumFactor(n);
      }
    };
  }

  public static ToDoubleFunction<RelativeMediumLayers> newSecondDerApparentByPhiKDivRho1(RelativeTetrapolarSystem system) {
    return new Apparent2Rho(new SecondDerivativeApparentByPhiK(system)) {
      @Override
      double kFactor(double k, @Nonnegative int n) {
        return pow(k, n - 1.0) * sumFactor(n);
      }
    };
  }

  public static ToDoubleFunction<RelativeMediumLayers> newSecondDerApparentByPhiPhiDivRho1(RelativeTetrapolarSystem system) {
    return new ToDoubleFunction<>() {
      private final ToDoubleFunction<RelativeMediumLayers> apparentByPhi2Rho = newDerApparentByPhiDivRho1(system);
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
      public double applyAsDouble(RelativeMediumLayers kw) {
        return apparentByPhi2Rho.applyAsDouble(kw) / kw.hToL().value() + secondPart.applyAsDouble(kw);
      }
    };
  }
}
