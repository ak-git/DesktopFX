package com.ak.rsm;

import java.util.Collection;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

enum Measurements {
  ;

  @Nonnegative
  static double getBaseL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream().mapToDouble(m -> m.getSystem().getL()).max().orElseThrow();
  }

  @Nonnegative
  static double getMaxHToL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream()
        .mapToDouble(measurement -> measurement.getSystem().getHMax(1.0)).min().orElseThrow() / getBaseL(measurements);
  }

  @Nonnull
  static ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted(@Nonnull Collection<? extends Measurement> measurements) {
    double baseL = getBaseL(measurements);
    return (s, kw) -> Apparent2Rho.newLog1pApparent2Rho(s.toRelative()).applyAsDouble(kw[0], kw[1] * baseL / s.getL());
  }

  @Nonnull
  static ToDoubleBiFunction<TetrapolarSystem, double[]> logDiffApparentPredicted(@Nonnull Collection<? extends Measurement> measurements) {
    double baseL = getBaseL(measurements);
    return (s, kw) -> log(Math.abs(Apparent2Rho.newDerivativeApparentByPhi2Rho(s.toRelative()).applyAsDouble(kw[0], kw[1] * baseL / s.getL())));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static ValuePair getRho1(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    if (RelativeMediumLayers.SINGLE_LAYER.equals(kw)) {
      Measurement average = measurements.stream().map(Measurement.class::cast).reduce(Measurement::merge).orElseThrow();
      double rho = average.getResistivity();
      return ValuePair.Name.RHO_1.of(rho, rho * average.getSystem().getApparentRelativeError());
    }
    else {
      double baseL = getBaseL(measurements);
      return measurements.stream().parallel()
          .map(measurement -> {
            TetrapolarSystem s = measurement.getSystem();
            double normApparent = Apparent2Rho.newNormalizedApparent2Rho(s.toRelative()).applyAsDouble(kw.k12(), kw.hToL() * baseL / s.getL());

            double fK = Math.abs(Apparent2Rho.newDerivativeApparentByK2Rho(s.toRelative()).applyAsDouble(kw.k12(), kw.hToL()) * kw.k12AbsError());
            double fPhi = Math.abs(Apparent2Rho.newDerivativeApparentByPhi2Rho(s.toRelative()).applyAsDouble(kw.k12(), kw.hToL()) * kw.hToLAbsError());

            return ValuePair.Name.RHO_1.of(measurement.getResistivity() / normApparent,
                (fK + fPhi) * measurement.getResistivity() / pow(normApparent, 2.0)
            );
          })
          .reduce(ValuePair::mergeWith).orElseThrow();
    }
  }
}
