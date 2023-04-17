package com.ak.rsm.inverse;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.ToDoubleFunction;

import static com.ak.util.Numbers.toInt;

abstract non-sealed class DynamicInverse extends AbstractInverseFunction<DerivativeResistivity> {
  private DynamicInverse(@Nonnull Collection<? extends DerivativeResistivity> r) {
    super(r, d -> d.resistivity() / d.derivativeResistivity());
  }

  static ToDoubleFunction<double[]> of(@Nonnull Collection<? extends DerivativeResistivity> r) {
    double dh = dH(r);
    StaticInverse staticInverse = new StaticInverse(r);
    return new DynamicInverse(r) {
      @Override
      @ParametersAreNonnullByDefault
      public double applyAsDouble(TetrapolarSystem s, double[] kw) {
        double dR = Apparent2Rho.newDerApparentByPhiDivRho1(s, dh).applyAsDouble(staticInverse.layer2RelativeMedium(s, kw));
        return staticInverse.applyAsDouble(s, kw) / dR;
      }
    };
  }

  static ToDoubleFunction<double[]> of(@Nonnull Collection<? extends DerivativeResistivity> r, @Nonnegative double hStep) {
    double dh = dH(r);
    return new DynamicInverse(r) {
      @Override
      @ParametersAreNonnullByDefault
      public double applyAsDouble(TetrapolarSystem s, double[] kw) {
        double dR = Apparent3Rho.newDerApparentByPhiDivRho1(s, new double[] {kw[0], kw[1]}, hStep, toInt(kw[2]), toInt(kw[3]), dh);
        double apparentPredicted = Apparent3Rho.newApparentDivRho1(s.relativeSystem())
            .value(kw[0], kw[1], hStep / s.lCC(), toInt(kw[2]), toInt(kw[3]));
        return apparentPredicted / dR;
      }
    };
  }

  private static double dH(@Nonnull Collection<? extends DerivativeResistivity> r) {
    return r.stream().mapToDouble(DerivativeResistivity::dh)
        .reduce((left, right) -> Double.compare(left, right) == 0 ? left : Double.NaN).orElse(Double.NaN);
  }
}
