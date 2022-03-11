package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.system.TetrapolarSystem;

import static java.lang.StrictMath.log;

final class Layer3DynamicInverse extends AbstractLayerInverse {
  Layer3DynamicInverse(@Nonnull Collection<TetrapolarSystem> systems, @Nonnegative double hStep) {
    super(systems, () -> {
      ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted = (s, kw) -> Apparent3Rho.newLog1pApparent3Rho(s.relativeSystem())
          .value(
              kw[0], kw[1],
              hStep / s.lCC(),
              (int) Math.round(kw[2]), (int) Math.round(kw[3])
          );
      ToDoubleBiFunction<TetrapolarSystem, double[]> logDiffApparentPredicted = (s, kw) ->
          log(
              Math.abs(
                  Apparent3Rho.newDerivativeApparentByPhi2Rho(s.relativeSystem())
                      .value(
                          kw[0], kw[1],
                          hStep / s.lCC(),
                          (int) Math.round(kw[2]), (int) Math.round(kw[3])
                      )
              )
          );
      return (s, kw) -> logApparentPredicted.applyAsDouble(s, kw) - logDiffApparentPredicted.applyAsDouble(s, kw);
    });
  }
}
