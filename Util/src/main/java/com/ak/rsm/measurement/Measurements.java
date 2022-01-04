package com.ak.rsm.measurement;

import java.util.Collection;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

public enum Measurements {
  ;

  @Nonnull
  public static Collection<TetrapolarSystem> toSystems(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.stream().map(Measurement::inexact).map(InexactTetrapolarSystem::system).toList();
  }

  @Nonnegative
  public static double getBaseL(@Nonnull Collection<TetrapolarSystem> systems) {
    return systems.stream().mapToDouble(TetrapolarSystem::lCC).max().orElseThrow();
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  public static ValuePair getRho1(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    if (RelativeMediumLayers.SINGLE_LAYER.equals(kw)) {
      Measurement average = measurements.stream().map(Measurement.class::cast).reduce(Measurement::merge).orElseThrow();
      double rho = average.resistivity();
      return ValuePair.Name.RHO_1.of(rho, rho * average.inexact().getApparentRelativeError());
    }
    else {
      double baseL = getBaseL(toSystems(measurements));
      return measurements.stream().parallel()
          .map(measurement -> {
            TetrapolarSystem s = measurement.inexact().system();
            double normApparent = Apparent2Rho.newNormalizedApparent2Rho(s.relativeSystem())
                .applyAsDouble(new Layer2RelativeMedium(kw.k12(), kw.hToL() * baseL / s.lCC()));

            double fK = Math.abs(Apparent2Rho.newDerivativeApparentByK2Rho(s.relativeSystem()).applyAsDouble(kw) * kw.k12AbsError());
            double fPhi = Math.abs(Apparent2Rho.newDerivativeApparentByPhi2Rho(s.relativeSystem()).applyAsDouble(kw) * kw.hToLAbsError());

            return ValuePair.Name.RHO_1.of(measurement.resistivity() / normApparent,
                (fK + fPhi) * measurement.resistivity() / pow(normApparent, 2.0)
            );
          })
          .reduce(ValuePair::mergeWith).orElseThrow();
    }
  }

  @Nonnegative
  public static double getMaxHToL(@Nonnull Collection<InexactTetrapolarSystem> systems) {
    return systems.parallelStream()
        .mapToDouble(s -> s.getHMax(1.0)).min().orElseThrow() / getBaseL(systems.stream().map(InexactTetrapolarSystem::system).toList());
  }

  @Nonnull
  public static ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted(@Nonnull Collection<TetrapolarSystem> systems) {
    double baseL = getBaseL(systems);
    return (s, kw) -> {
      RelativeMediumLayers relativeMedium = new Layer2RelativeMedium(kw[0], kw[1] * baseL / s.lCC());
      return Apparent2Rho.newLog1pApparent2Rho(s.relativeSystem()).applyAsDouble(relativeMedium);
    };
  }

  @Nonnull
  public static ToDoubleBiFunction<TetrapolarSystem, double[]> logDiffApparentPredicted(@Nonnull Collection<TetrapolarSystem> measurements) {
    double baseL = getBaseL(measurements);
    return (s, kw) -> {
      RelativeMediumLayers relativeMedium = new Layer2RelativeMedium(kw[0], kw[1] * baseL / s.lCC());
      return log(Math.abs(Apparent2Rho.newDerivativeApparentByPhi2Rho(s.relativeSystem()).applyAsDouble(relativeMedium)));
    };
  }
}

