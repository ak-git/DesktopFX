package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.system.InexactTetrapolarSystem;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

abstract sealed class AbstractRelative<M extends Measurement, L> extends AbstractErrors implements Inverse<L>, ToDoubleFunction<double[]>
    permits DynamicRelative, StaticRelative {
  @Nonnull
  private final Collection<M> measurements;
  @Nonnull
  private final ToDoubleFunction<double[]> inverse;
  @Nonnull
  private final Regularization regularization;

  @ParametersAreNonnullByDefault
  AbstractRelative(Collection<? extends M> measurements,
                   ToDoubleFunction<double[]> inverse,
                   Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
    super(measurements.stream().map(Measurement::inexact).toList());
    this.measurements = Collections.unmodifiableCollection(measurements);
    this.inverse = inverse;
    regularization = regularizationFunction.apply(inexactSystems());
  }

  @Override
  public double applyAsDouble(@Nonnull double[] value) {
    return inverse.applyAsDouble(value);
  }

  @Nonnull
  final Collection<M> measurements() {
    return measurements;
  }

  @Nonnull
  final Regularization regularization() {
    return regularization;
  }
}
