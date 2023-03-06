package com.ak.rsm.inverse;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.system.TetrapolarSystem;
import org.apache.commons.math3.complex.Complex;

import javax.annotation.Nonnull;
import java.util.Collection;

import static java.lang.StrictMath.log;

final class Layer2DynamicInverse extends AbstractLayerInverse {
  Layer2DynamicInverse(@Nonnull Collection<TetrapolarSystem> systems, double dh) {
    super(systems, () -> {
      Layer2StaticInverse logApparentPredicted = new Layer2StaticInverse(systems);
      return (s, kw) -> {
        double dR = Apparent2Rho.newDerivativeApparentByPhiDivRho1(s, dh)
            .applyAsDouble(logApparentPredicted.layersBiFunction().apply(s, kw));
        double log = logApparentPredicted.apply(s, kw).getReal() - log(Math.abs(dR));
        return new Complex(log, log * Math.signum(dR));
      };
    });
  }
}
