package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

final class DynamicRelative extends AbstractRelative<DerivativeMeasurement> {
  @ParametersAreNonnullByDefault
  DynamicRelative(Collection<? extends DerivativeMeasurement> measurements,
                  Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
    super(measurements, DynamicInverse.of(measurements), regularizationFunction);
  }

  @Nonnull
  @Override
  public RelativeMediumLayers get() {
    Predicate<DerivativeMeasurement> gtZero = d -> d.derivativeResistivity() > 0;
    Predicate<DerivativeMeasurement> ltZero = d -> d.derivativeResistivity() < 0;
    if (measurements().stream().allMatch(gtZero) || measurements().stream().allMatch(ltZero)) {
      return super.get();
    }
    else if (measurements().stream().anyMatch(gtZero) && measurements().stream().anyMatch(ltZero)) {
      return RelativeMediumLayers.NAN;
    }
    return new StaticRelative(measurements(), regularizationFunction()).get();
  }

  @Override
  Simplex.Bounds kInterval() {
    Simplex.Bounds kMinMax = super.kInterval();
    if (measurements().stream().allMatch(d -> d.derivativeResistivity() > 0)) {
      kMinMax = new Simplex.Bounds(-1.0, 0.0);
    }
    else if (measurements().stream().allMatch(d -> d.derivativeResistivity() < 0)) {
      kMinMax = new Simplex.Bounds(0.0, 1.0);
    }
    return kMinMax;
  }
}
