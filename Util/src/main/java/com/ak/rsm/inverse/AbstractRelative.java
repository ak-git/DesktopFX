package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.measurement.Measurement;

abstract class AbstractRelative<M extends Measurement, L> extends AbstractErrors implements Inverse<L> {
  @Nonnull
  private final Collection<M> measurements;
  @Nonnegative
  private final double maxHToL;

  AbstractRelative(@Nonnull Collection<? extends M> measurements) {
    super(measurements.stream().map(Measurement::inexact).toList());
    this.measurements = Collections.unmodifiableCollection(measurements);
    maxHToL = inexactSystems().parallelStream().mapToDouble(s -> s.getHMax(1.0)).min().orElseThrow() / baseL();
  }

  @Nonnegative
  final double getMaxHToL() {
    return maxHToL;
  }

  @Nonnull
  final Collection<M> measurements() {
    return measurements;
  }
}
