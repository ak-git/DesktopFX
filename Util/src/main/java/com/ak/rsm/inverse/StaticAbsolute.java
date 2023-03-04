package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.medium.Layer1Medium;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.medium.MediumLayers;
import com.ak.rsm.relative.RelativeMediumLayers;

import javax.annotation.Nonnull;
import java.util.Collection;

import static com.ak.rsm.inverse.StaticErrors.SUBTRACT;

final class StaticAbsolute implements Inverse<MediumLayers> {
  @Nonnull
  private final StaticRelative inverseRelative;

  StaticAbsolute(@Nonnull Collection<? extends Measurement> measurements) {
    inverseRelative = new StaticRelative(measurements, SUBTRACT);
  }

  @Nonnull
  @Override
  public MediumLayers get() {
    Collection<Measurement> measurements = inverseRelative.measurements();
    if (measurements.size() > 2) {
      return new Layer2Medium(measurements, inverseRelative.get());
    }
    else {
      return new Layer1Medium(measurements);
    }
  }

  @Nonnull
  @Override
  public RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    return inverseRelative.apply(layers);
  }
}
