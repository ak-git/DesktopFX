package com.ak.rsm.inverse;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.medium.Layer1Medium;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.medium.MediumLayers;
import com.ak.rsm.relative.RelativeMediumLayers;

import static com.ak.rsm.inverse.StaticRelative.SUBTRACT;

class StaticAbsolute extends AbstractInverse<Measurement, MediumLayers> {
  @Nonnull
  private final StaticRelative inverseRelative;

  StaticAbsolute(@Nonnull Collection<? extends Measurement> measurements) {
    super(measurements);
    inverseRelative = new StaticRelative(measurements);
  }

  @Nonnull
  @Override
  public RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    return inverseRelative.apply(layers);
  }

  @Nonnull
  @Override
  public MediumLayers get() {
    if (measurements().size() > 2) {
      return new Layer2Medium(measurements(), inverseRelative.inverseRelative(SUBTRACT));
    }
    else {
      return new Layer1Medium(measurements());
    }
  }
}
