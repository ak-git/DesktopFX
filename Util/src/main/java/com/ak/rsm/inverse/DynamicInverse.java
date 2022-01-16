package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnull;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.TetrapolarSystem;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.log;

final class DynamicInverse extends AbstractInverseFunction {
  @Nonnull
  private final ToDoubleBiFunction<TetrapolarSystem, double[]> logDiffApparentPredicted = (s, kw) ->
      log(
          Math.abs(
              Apparent2Rho.newDerivativeApparentByPhi2Rho(s.relativeSystem()).applyAsDouble(layersBiFunction().apply(s, kw))
          )
      );

  DynamicInverse(@Nonnull Collection<? extends DerivativeResistivity> r) {
    super(r, r.stream().mapToDouble(d -> log(d.resistivity()) - log(abs(d.derivativeResistivity()))).toArray());
  }

  @Override
  public double[] apply(double[] kw) {
    return systems().stream()
        .mapToDouble(s -> logApparentPredicted().applyAsDouble(s, kw) - logDiffApparentPredicted.applyAsDouble(s, kw))
        .toArray();
  }
}
