package com.ak.rsm.inverse;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

import java.util.Collection;

final class StaticInverse extends AbstractInverseFunction<Resistivity> {
  StaticInverse(Collection<? extends Measurement> r) {
    super(r, Resistivity::resistivity, Errors.Builder.STATIC.of(Measurement.toInexact(r)));
  }

  @Override
  public double applyAsDouble(TetrapolarSystem s, double[] kw) {
    return Apparent2Rho.newApparentDivRho1(s.relativeSystem()).applyAsDouble(layer2RelativeMedium(s, kw));
  }
}
