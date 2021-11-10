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
    return measurements.stream().mapToDouble(m -> m.system().getL()).max().orElseThrow();
  }

  @Nonnegative
  static double getMaxHToL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream()
        .mapToDouble(measurement -> measurement.system().getHMax(1.0)).min().orElseThrow() / getBaseL(measurements);
  }

  @Nonnull
  static ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted(@Nonnull Collection<? extends Measurement> measurements) {
    double baseL = getBaseL(measurements);
    return (s, kw) -> {
      RelativeMediumLayers relativeMedium = new Layer2RelativeMedium(kw[0], kw[1] * baseL / s.getL());
      return Apparent2Rho.newLog1pApparent2Rho(s.toRelative()).applyAsDouble(relativeMedium);
    };
  }

  @Nonnull
  static ToDoubleBiFunction<TetrapolarSystem, double[]> logDiffApparentPredicted(@Nonnull Collection<? extends Measurement> measurements) {
    double baseL = getBaseL(measurements);
    return (s, kw) -> {
      RelativeMediumLayers relativeMedium = new Layer2RelativeMedium(kw[0], kw[1] * baseL / s.getL());
      return log(Math.abs(Apparent2Rho.newDerivativeApparentByPhi2Rho(s.toRelative()).applyAsDouble(relativeMedium)));
    };
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static ValuePair getRho1(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    if (RelativeMediumLayers.SINGLE_LAYER.equals(kw)) {
      Measurement average = measurements.stream().map(Measurement.class::cast).reduce(Measurement::merge).orElseThrow();
      double rho = average.resistivity();
      return ValuePair.Name.RHO_1.of(rho, rho * average.system().getApparentRelativeError());
    }
    else {
      double baseL = getBaseL(measurements);
      return measurements.stream().parallel()
          .map(measurement -> {
            TetrapolarSystem s = measurement.system();
            double normApparent = Apparent2Rho.newNormalizedApparent2Rho(s.toRelative()).applyAsDouble(new Layer2RelativeMedium(kw.k12(), kw.hToL() * baseL / s.getL()));

            double fK = Math.abs(Apparent2Rho.newDerivativeApparentByK2Rho(s.toRelative()).applyAsDouble(kw) * kw.k12AbsError());
            double fPhi = Math.abs(Apparent2Rho.newDerivativeApparentByPhi2Rho(s.toRelative()).applyAsDouble(kw) * kw.hToLAbsError());

            return ValuePair.Name.RHO_1.of(measurement.resistivity() / normApparent,
                (fK + fPhi) * measurement.resistivity() / pow(normApparent, 2.0)
            );
          })
          .reduce(ValuePair::mergeWith).orElseThrow();
    }
  }
}
