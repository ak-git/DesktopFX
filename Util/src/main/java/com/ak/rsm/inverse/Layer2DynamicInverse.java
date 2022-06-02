package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnull;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import org.apache.commons.math3.complex.Complex;

import static java.lang.StrictMath.log;

final class Layer2DynamicInverse extends AbstractLayerInverse {
  Layer2DynamicInverse(@Nonnull Collection<TetrapolarSystem> systems, double dh) {
    super(systems, () -> {
      Layer2StaticInverse logApparentPredicted = new Layer2StaticInverse(systems);
      ToDoubleBiFunction<TetrapolarSystem, double[]> diffApparentPredicted = Double.isNaN(dh) ?
          (s, kw) ->
              Apparent2Rho.newDerivativeApparentByPhi2Rho(s.relativeSystem())
                  .applyAsDouble(logApparentPredicted.layersBiFunction().apply(s, kw))
          :
          (s, kw) -> {
            RelativeMediumLayers kh = logApparentPredicted.layersBiFunction().apply(s, kw);
            double rho1 = 1.0;
            double rho2 = rho1 / Layers.getRho1ToRho2(kh.k12());
            return TetrapolarDerivativeResistance.of(s).dh(dh).rho1(rho1).rho2(rho2).h(kh.hToL() * s.lCC()).derivativeResistivity();
          };
      return (s, kw) -> {
        double dR = diffApparentPredicted.applyAsDouble(s, kw);
        double log = logApparentPredicted.apply(s, kw).getReal() - log(Math.abs(dR));
        return new Complex(log, log * Math.signum(dR));
      };
    });
  }
}
