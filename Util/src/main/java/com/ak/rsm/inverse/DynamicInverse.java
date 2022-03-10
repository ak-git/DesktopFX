package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.ToDoubleBiFunction;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.TetrapolarSystem;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.log;

final class DynamicInverse extends AbstractInverseFunction<DerivativeResistivity> {
  @Nonnull
  private final StaticInverse staticInverse;
  @Nonnull
  private final ToDoubleBiFunction<TetrapolarSystem, double[]> logDiffApparentPredicted;

  DynamicInverse(@Nonnull Collection<? extends DerivativeResistivity> r) {
    super(r, d -> log(d.resistivity()) - log(abs(d.derivativeResistivity())), UnaryOperator.identity());
    staticInverse = new StaticInverse(r, UnaryOperator.identity());
    logDiffApparentPredicted = (s, kw) ->
        log(
            Math.abs(
                Apparent2Rho.newDerivativeApparentByPhi2Rho(s.relativeSystem()).applyAsDouble(staticInverse.layersBiFunction().apply(s, kw))
            )
        );
  }

  @Override
  @ParametersAreNonnullByDefault
  public double applyAsDouble(TetrapolarSystem s, double[] kw) {
    return staticInverse.applyAsDouble(s, kw) - logDiffApparentPredicted.applyAsDouble(s, kw);
  }
}
