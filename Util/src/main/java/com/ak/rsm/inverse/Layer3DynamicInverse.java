package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.system.TetrapolarSystem;
import org.apache.commons.math3.complex.Complex;

import static com.ak.util.Numbers.toInt;
import static java.lang.StrictMath.log;

final class Layer3DynamicInverse extends AbstractLayerInverse {
  Layer3DynamicInverse(@Nonnull Collection<TetrapolarSystem> systems, @Nonnegative double hStep, double dh) {
    super(systems, () -> {
      ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted = (s, kw) ->
          Apparent3Rho.newLog1pApparent3Rho(s.relativeSystem())
              .value(kw[0], kw[1], hStep / s.lCC(), toInt(kw[2]), toInt(kw[3]));
      ToDoubleBiFunction<TetrapolarSystem, double[]> diffApparentPredicted = (s, kw) ->
          Apparent3Rho.newDerivativeApparentByPhi2Rho(s, new double[] {kw[0], kw[1]}, hStep, toInt(kw[2]), toInt(kw[3]), dh);
      return (s, kw) -> {
        double dR = diffApparentPredicted.applyAsDouble(s, kw);
        double real = logApparentPredicted.applyAsDouble(s, kw) - log(Math.abs(dR));
        return new Complex(real, real * Math.signum(dR));
      };
    });
  }
}
