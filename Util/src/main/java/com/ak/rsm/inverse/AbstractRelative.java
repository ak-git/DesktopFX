package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.function.ToDoubleFunction;

abstract sealed class AbstractRelative<M extends Measurement, L> extends AbstractErrors implements Inverse<L>, ToDoubleFunction<double[]>
    permits DynamicRelative, StaticRelative {
  @Nonnull
  private final Collection<M> measurements;
  @Nonnull
  private final ToDoubleFunction<double[]> inverse;

  @ParametersAreNonnullByDefault
  AbstractRelative(Collection<? extends M> measurements, ToDoubleFunction<double[]> inverse) {
    super(measurements.stream().map(Measurement::inexact).toList());
    this.measurements = Collections.unmodifiableCollection(measurements);
    this.inverse = inverse;
  }

  @Override
  public final double applyAsDouble(@Nonnull double[] value) {
    return inverse.applyAsDouble(value);
  }

  @Nonnull
  final Collection<M> measurements() {
    return measurements;
  }
}
