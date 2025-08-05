package com.ak.rsm.inverse;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.resistance.DeltaH;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.TetrapolarSystem;

import java.util.Collection;
import java.util.function.ToDoubleBiFunction;

import static com.ak.util.Numbers.toInt;

abstract class DynamicInverse extends AbstractInverseFunction<DerivativeResistivity> {
  private DynamicInverse(Collection<? extends DerivativeMeasurement> r) {
    super(r, d -> d.resistivity() / d.derivativeResistivity(), Errors.Builder.DYNAMIC.of(Measurement.toInexact(r)));
  }

  static InverseFunction of(Collection<? extends DerivativeMeasurement> r) {
    double dh = dH(r);
    ToDoubleBiFunction<TetrapolarSystem, double[]> staticInverse = new StaticInverse(r);
    return new DynamicInverse(r) {
      @Override
      public double applyAsDouble(TetrapolarSystem s, double[] kw) {
        double dR = Apparent2Rho.newDerApparentByPhiDivRho1(s, dh).applyAsDouble(layer2RelativeMedium(s, kw));
        return staticInverse.applyAsDouble(s, kw) / dR;
      }
    };
  }

  static InverseFunction ofH1Changed(Collection<? extends DerivativeMeasurement> r, double hStep) {
    return of(r, hStep, DeltaH.H1.apply(dH(r)));
  }

  static InverseFunction ofH2Changed(Collection<? extends DerivativeMeasurement> r, double hStep) {
    return of(r, hStep, DeltaH.H2.apply(dH(r)));
  }

  private static InverseFunction of(Collection<? extends DerivativeMeasurement> r, double hStep, DeltaH deltaH) {
    return new DynamicInverse(r) {
      @Override
      public double applyAsDouble(TetrapolarSystem s, double[] kw) {
        double dR = Apparent3Rho.newDerApparentByPhiDivRho1(
            s, new double[] {kw[0], kw[1]}, hStep, toInt(kw[2]), toInt(kw[3]), deltaH);
        double apparentPredicted = Apparent3Rho.newApparentDivRho1(s.relativeSystem())
            .value(kw[0], kw[1], hStep / s.lCC(), toInt(kw[2]), toInt(kw[3]));
        return apparentPredicted / dR;
      }
    };
  }

  private static double dH(Collection<? extends DerivativeResistivity> r) {
    return r.stream().mapToDouble(DerivativeResistivity::dh)
        .reduce((left, right) -> Double.compare(left, right) == 0 ? left : Double.NaN).orElse(Double.NaN);
  }
}
