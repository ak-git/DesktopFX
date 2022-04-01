package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.DoubleToIntFunction;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.system.TetrapolarSystem;

import static java.lang.StrictMath.log;

final class Layer3DynamicInverse extends AbstractLayerInverse {
  private static final DoubleToIntFunction ROUND_TO_INT = value -> Math.toIntExact(Math.round(value));

  Layer3DynamicInverse(@Nonnull Collection<TetrapolarSystem> systems, @Nonnegative double hStep) {
    super(systems, () -> {
      ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted = (s, kw) ->
          Apparent3Rho.newLog1pApparent3Rho(s.relativeSystem())
              .value(kw[0], kw[1], hStep / s.lCC(), ROUND_TO_INT.applyAsInt(kw[2]), ROUND_TO_INT.applyAsInt(kw[3]));
      ToDoubleBiFunction<TetrapolarSystem, double[]> diffApparentPredicted = (s, kw) ->
          Apparent3Rho.newDerivativeApparentByPhi2Rho(s, new double[] {kw[0], kw[1]}, hStep, ROUND_TO_INT
              .applyAsInt(kw[2]), ROUND_TO_INT.applyAsInt(kw[3]));
      return (s, kw) -> {
        double dR = diffApparentPredicted.applyAsDouble(s, kw);
        return Math.signum(dR) * (logApparentPredicted.applyAsDouble(s, kw) - log(Math.abs(dR)));
      };
    });
  }
}
