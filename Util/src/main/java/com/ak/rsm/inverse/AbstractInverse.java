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
import com.ak.rsm.system.TetrapolarSystem;

import static java.lang.StrictMath.log;

abstract class AbstractInverse<M extends Measurement, L> extends AbstractErrors implements Inverse<L> {
  @Nonnull
  private final Collection<M> measurements;
  @Nonnegative
  private final double maxHToL;
  @Nonnull
  private final BiFunction<TetrapolarSystem, double[], RelativeMediumLayers> layersBiFunction;

  AbstractInverse(@Nonnull Collection<? extends M> measurements) {
    super(measurements.stream().map(Measurement::inexact).toList());
    this.measurements = Collections.unmodifiableCollection(measurements);
    maxHToL = inexactSystems().parallelStream().mapToDouble(s -> s.getHMax(1.0)).min().orElseThrow() / baseL();
    layersBiFunction = (s1, kw1) -> new Layer2RelativeMedium(kw1[0], kw1[1] * baseL() / s1.lCC());
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
