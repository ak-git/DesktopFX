package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.system.InexactTetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.OptionalDouble;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.DoubleStream;

abstract sealed class AbstractRelative<M extends Measurement, L> extends AbstractErrors implements Inverse<L>
    permits DynamicRelative, StaticRelative {
  @Nonnull
  private final Collection<M> measurements;
  private final DoubleUnaryOperator max = newMergeHorizons(InexactTetrapolarSystem::getHMax, DoubleStream::min);
  private final DoubleUnaryOperator min = newMergeHorizons(InexactTetrapolarSystem::getHMin, DoubleStream::max);

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
                                               Function<DoubleStream, OptionalDouble> selector) {
    return k -> selector
        .apply(inexactSystems().stream().mapToDouble(system -> toHorizon.applyAsDouble(system, k)))
        .orElseThrow() / baseL();
  }
}
