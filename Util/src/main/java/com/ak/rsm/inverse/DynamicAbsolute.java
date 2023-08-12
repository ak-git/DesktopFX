package com.ak.rsm.inverse;

import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.Function;

final class DynamicAbsolute implements Inverse<Layer2Medium> {
  @Nonnull
  private final DynamicRelative inverseRelative;

  @ParametersAreNonnullByDefault
  DynamicAbsolute(Collection<? extends DerivativeMeasurement> measurements,
                  Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
    inverseRelative = new DynamicRelative(measurements, regularizationFunction);
  }

  @Nonnull
  @Override
  public Layer2Medium get() {
    var measurements = inverseRelative.measurements();
    return new Layer2Medium(measurements, measurements.size() > 1 ? inverseRelative.get() : RelativeMediumLayers.SINGLE_LAYER);
  }

  @Nonnull
  @Override
  public RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    return inverseRelative.apply(layers);
  }
}
