package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnull;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.system.TetrapolarSystem;

import static java.lang.StrictMath.log;

final class Layer2DynamicInverse extends AbstractLayerInverse {
  Layer2DynamicInverse(@Nonnull Collection<TetrapolarSystem> systems) {
    super(systems, () -> {
      Layer2StaticInverse logApparentPredicted = new Layer2StaticInverse(systems);
      ToDoubleBiFunction<TetrapolarSystem, double[]> diffApparentPredicted = (s, kw) ->
          Apparent2Rho.newDerivativeApparentByPhi2Rho(s.relativeSystem())
              .applyAsDouble(logApparentPredicted.layersBiFunction().apply(s, kw));

      return (s, kw) -> {
        double dR = diffApparentPredicted.applyAsDouble(s, kw);
        return Math.signum(dR) * (logApparentPredicted.applyAsDouble(s, kw) - log(Math.abs(dR)));
      };
    });
  }
}
