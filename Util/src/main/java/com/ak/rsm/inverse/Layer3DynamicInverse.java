package com.ak.rsm.inverse;

import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;

import static com.ak.util.Numbers.toInt;

final class Layer3DynamicInverse extends AbstractLayerInverse {
  Layer3DynamicInverse(@Nonnull Collection<TetrapolarSystem> systems, @Nonnegative double hStep, double dh) {
    super(systems, () -> (s, kw) -> {
      double dR = Apparent3Rho.newDerApparentByPhiDivRho1(s, new double[] {kw[0], kw[1]}, hStep, toInt(kw[2]), toInt(kw[3]), dh);
      double apparentPredicted = Apparent3Rho.newApparentDivRho1(s.relativeSystem())
          .value(kw[0], kw[1], hStep / s.lCC(), toInt(kw[2]), toInt(kw[3]));
      return apparentPredicted / dR;
    });
  }
}
