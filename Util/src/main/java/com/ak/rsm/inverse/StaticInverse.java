package com.ak.rsm.inverse;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

final class StaticInverse extends AbstractInverseFunction<Resistivity> {
  StaticInverse(@Nonnull Collection<? extends Measurement> r) {
    super(r, Resistivity::resistivity, new StaticErrors(Measurement.inexact(r)));
  }

  @Override
  @ParametersAreNonnullByDefault
  public double applyAsDouble(TetrapolarSystem s, double[] kw) {
    return Apparent2Rho.newApparentDivRho1(s.relativeSystem()).applyAsDouble(layer2RelativeMedium(s, kw));
  }
}
