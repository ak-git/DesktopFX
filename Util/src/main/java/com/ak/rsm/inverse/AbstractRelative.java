package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.system.InexactTetrapolarSystem;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

abstract sealed class AbstractRelative<M extends Measurement, L> extends AbstractErrors implements Inverse<L>
    permits DynamicRelative, StaticRelative {
  @Nonnull
  private final Collection<M> measurements;
  @Nonnull
  private final Regularization regularization;


  @ParametersAreNonnullByDefault
  AbstractRelative(Collection<? extends M> measurements,
                   Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
    super(measurements.stream().map(Measurement::inexact).toList());
    this.measurements = Collections.unmodifiableCollection(measurements);
    regularization = regularizationFunction.apply(inexactSystems());
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
