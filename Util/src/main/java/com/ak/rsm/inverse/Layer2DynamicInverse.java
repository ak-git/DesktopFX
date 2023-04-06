package com.ak.rsm.inverse;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnull;
import java.util.Collection;

final class Layer2DynamicInverse extends AbstractLayerInverse {
  Layer2DynamicInverse(@Nonnull Collection<TetrapolarSystem> systems, double dh) {
    super(systems, () -> {
      Layer2StaticInverse apparentPredicted = new Layer2StaticInverse(systems);
      return (s, kw) -> {
        double dR = Apparent2Rho.newDerApparentByPhiDivRho1(s, dh).applyAsDouble(apparentPredicted.layersBiFunction().apply(s, kw));
        return apparentPredicted.applyAsDouble(s, kw) / dR;
      };
    });
  }
}
