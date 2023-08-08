package com.ak.rsm.inverse;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

final class StaticInverse extends AbstractInverseFunction<Resistivity> {
  @Nonnegative
  private final double baseL;

  StaticInverse(@Nonnull Collection<? extends Resistivity> r) {
    super(r, Resistivity::resistivity);
    baseL = Resistivity.getBaseL(r);
  }

  @Override
  @ParametersAreNonnullByDefault
  public double applyAsDouble(TetrapolarSystem s, double[] kw) {
    return Apparent2Rho.newApparentDivRho1(s.relativeSystem()).applyAsDouble(layer2RelativeMedium(s, kw));
  }

  @ParametersAreNonnullByDefault
  RelativeMediumLayers layer2RelativeMedium(TetrapolarSystem s, double[] kw) {
    return new Layer2RelativeMedium(kw[0], kw[1] * baseL / s.lCC());
  }
}
