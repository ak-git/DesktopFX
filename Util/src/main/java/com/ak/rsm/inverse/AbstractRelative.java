package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

abstract sealed class AbstractRelative<M extends Measurement, L> implements Inverse<L>, ToDoubleFunction<double[]>
    permits DynamicRelative, StaticRelative {
  @Nonnull
  private final Collection<M> measurements;
  @Nonnull
  private final ToDoubleFunction<double[]> inverse;
  @Nonnull
  private final Regularization regularization;
  @Nonnull
  private final UnaryOperator<RelativeMediumLayers> errors;

  @ParametersAreNonnullByDefault
  AbstractRelative(Collection<? extends M> measurements, ToDoubleFunction<double[]> inverse,
                   Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction,
                   UnaryOperator<RelativeMediumLayers> errors) {
    this.measurements = Collections.unmodifiableCollection(measurements);
    this.inverse = inverse;
    regularization = regularizationFunction.apply(Measurement.inexact(measurements));
    this.errors = errors;
  }

  @Override
  public final double applyAsDouble(@Nonnull double[] value) {
    return inverse.applyAsDouble(value);
  }

  @Nonnull
  @Override
  public final RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    return errors.apply(layers);
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
