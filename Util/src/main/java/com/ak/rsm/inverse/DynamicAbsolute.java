package com.ak.rsm.inverse;

import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.medium.MediumLayers;
import com.ak.rsm.relative.RelativeMediumLayers;

import javax.annotation.Nonnull;
import java.util.Collection;

final class DynamicAbsolute implements Inverse<MediumLayers> {
  @Nonnull
  private final DynamicRelative inverseRelative;

  DynamicAbsolute(@Nonnull Collection<? extends DerivativeMeasurement> measurements) {
    inverseRelative = new DynamicRelative(measurements);
  }

  @Nonnull
  @Override
  public MediumLayers get() {
    Collection<DerivativeMeasurement> measurements = inverseRelative.measurements();
    if (measurements.size() > 1) {
      return new Layer2Medium(measurements, inverseRelative.get());
    }
    else {
      return new StaticAbsolute(measurements).get();
    }
  }

  @Nonnull
  @Override
  public RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    return inverseRelative.apply(layers);
  }
}
