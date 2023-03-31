package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

abstract sealed class AbstractRelative<M extends Measurement, L> extends AbstractErrors implements Inverse<L>
    permits DynamicRelative, StaticRelative {
  @Nonnull
  private final Collection<M> measurements;

  AbstractRelative(@Nonnull Collection<? extends M> measurements) {
    super(measurements.stream().map(Measurement::inexact).toList());
    this.measurements = Collections.unmodifiableCollection(measurements);
  }

  @Nonnull
  final Collection<M> measurements() {
    return measurements;
  }
}
