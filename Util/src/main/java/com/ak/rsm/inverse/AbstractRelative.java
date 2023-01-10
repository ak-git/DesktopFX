package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.system.InexactTetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

abstract class AbstractRelative<M extends Measurement, L> extends AbstractErrors implements Inverse<L> {
  @Nonnull
  private final Collection<M> measurements;
  private final DoubleUnaryOperator max = newMergeHorizons(InexactTetrapolarSystem::getHMax, value -> value.max().orElseThrow());
  private final DoubleUnaryOperator min = newMergeHorizons(InexactTetrapolarSystem::getHMin, value -> value.min().orElseThrow());

  AbstractRelative(@Nonnull Collection<? extends M> measurements) {
    super(measurements.stream().map(Measurement::inexact).toList());
    this.measurements = Collections.unmodifiableCollection(measurements);
  }

  @Nonnegative
  final double getMaxHToL(double k) {
    return max.applyAsDouble(k);
  }

  @Nonnegative
  final double getMinHToL(double k) {
    return min.applyAsDouble(k);
  }

  @Nonnull
  final Collection<M> measurements() {
    return measurements;
  }

  @ParametersAreNonnullByDefault
  private DoubleUnaryOperator newMergeHorizons(ToDoubleBiFunction<InexactTetrapolarSystem, Double> toHorizon,
                                               ToDoubleFunction<DoubleStream> selector) {
    return k -> selector.applyAsDouble(
        inexactSystems().stream().mapToDouble(system -> toHorizon.applyAsDouble(system, k))
    ) / baseL();
  }
}
