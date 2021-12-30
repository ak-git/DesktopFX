package com.ak.rsm.medium;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.relative.RelativeMediumLayers;

final class Layer1Medium extends AbstractMediumLayers {
  Layer1Medium(@Nonnull Collection<? extends Measurement> measurements) {
    super(measurements, RelativeMediumLayers.SINGLE_LAYER);
  }

  @Override
  public ValuePair h1() {
    return ValuePair.Name.H.of(Double.NaN, 0.0);
  }

  @Override
  public String toString() {
    return "%s; %s".formatted(rho(), super.toString());
  }
}

