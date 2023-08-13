package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import org.apache.commons.math3.optim.PointValuePair;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.StrictMath.hypot;

abstract sealed class AbstractRelative<M extends Measurement> implements Supplier<RelativeMediumLayers>
    permits DynamicRelative, StaticRelative {
  @Nonnull
  private final Collection<M> measurements;
  @Nonnull
  private final InverseFunction inverse;
  @Nonnull
  private final Regularization regularization;

  @ParametersAreNonnullByDefault
  AbstractRelative(Collection<? extends M> measurements, InverseFunction inverse,
                   Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
    this.measurements = Collections.unmodifiableCollection(measurements);
    this.inverse = inverse;
    regularization = regularizationFunction.apply(Measurement.inexact(measurements));
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public RelativeMediumLayers get() {
    PointValuePair kwOptimal = Simplex.optimizeAll(kw -> {
          double regularizing = regularization.of(kw);
          if (Double.isFinite(regularizing)) {
            return hypot(inverse.applyAsDouble(kw), regularizing);
          }
          return regularizing;
        },
        kInterval(), regularization.hInterval(1.0)
    );
    return inverse.apply(new RelativeMediumLayers(kwOptimal.getPoint()));
  }

  @Nonnull
  final Collection<M> measurements() {
    return measurements;
  }

  @OverridingMethodsMustInvokeSuper
  Simplex.Bounds kInterval() {
    return new Simplex.Bounds(-1.0, 1.0);
  }
}
