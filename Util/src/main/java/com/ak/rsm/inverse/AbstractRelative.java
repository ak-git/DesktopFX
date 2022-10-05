package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.measurement.Measurement;

abstract class AbstractRelative<M extends Measurement, L> extends AbstractErrors implements Inverse<L> {
  @Nonnull
  private final Collection<M> measurements;

  AbstractRelative(@Nonnull Collection<? extends M> measurements) {
    super(measurements.stream().map(Measurement::inexact).toList());
    this.measurements = Collections.unmodifiableCollection(measurements);
  }

  @Nonnegative
  final double getMaxHToL(double k) {
    return inexactSystems().parallelStream().mapToDouble(s -> s.getHMax(k)).min().orElseThrow() / baseL();
  }

  @Nonnegative
  final double getMinHToL(double k) {
    return inexactSystems().parallelStream().mapToDouble(s -> s.getHMin(k)).max().orElseThrow() / baseL();
  }

  @Nonnull
  final Collection<M> measurements() {
    return measurements;
  }
}
