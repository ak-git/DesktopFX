package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.ToDoubleBiFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;

import static com.ak.rsm.measurement.Measurements.getBaseL;
import static java.lang.StrictMath.log;

abstract class AbstractInverse<M extends Measurement, L> implements Inverse<L> {
  @Nonnull
  private final Collection<M> measurements;
  @Nonnull
  private final Collection<InexactTetrapolarSystem> inexactSystems;
  @Nonnull
  private final Collection<TetrapolarSystem> systems;
  @Nonnegative
  private final double baseL;
  @Nonnegative
  private final double maxHToL;
  @Nonnull
  private final BiFunction<TetrapolarSystem, double[], RelativeMediumLayers> layersBiFunction;

  AbstractInverse(@Nonnull Collection<? extends M> measurements) {
    this.measurements = Collections.unmodifiableCollection(measurements);
    inexactSystems = Collections.unmodifiableCollection(measurements.stream().map(Measurement::inexact).toList());
    systems = Collections.unmodifiableCollection(inexactSystems.stream().map(InexactTetrapolarSystem::system).toList());
    baseL = getBaseL(systems);
    maxHToL = inexactSystems.parallelStream().mapToDouble(s -> s.getHMax(1.0)).min().orElseThrow() / baseL;
    layersBiFunction = (s1, kw1) -> new Layer2RelativeMedium(kw1[0], kw1[1] * baseL / s1.lCC());
  }

  @Nonnegative
  final double getMaxHToL() {
    return maxHToL;
  }

  @Nonnull
  final Collection<M> measurements() {
    return measurements;
  }

  @Nonnull
  final Collection<InexactTetrapolarSystem> inexactSystems() {
    return inexactSystems;
  }

  @Nonnull
  final Collection<TetrapolarSystem> systems() {
    return systems;
  }

  @Nonnull
  ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted() {
    return (s, kw) -> Apparent2Rho.newLog1pApparent2Rho(s.relativeSystem()).applyAsDouble(layersBiFunction.apply(s, kw));
  }

  @Nonnull
  final ToDoubleBiFunction<TetrapolarSystem, double[]> logDiffApparentPredicted() {
    return (s, kw) ->
        log(
            Math.abs(
                Apparent2Rho.newDerivativeApparentByPhi2Rho(s.relativeSystem()).applyAsDouble(layersBiFunction.apply(s, kw))
            )
        );
  }
}
