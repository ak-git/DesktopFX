package com.ak.rsm.medium;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.relative.Layer1RelativeMedium;

import javax.annotation.Nonnull;
import java.util.Collection;

public final class Layer1Medium extends AbstractMediumLayers {
  public Layer1Medium(@Nonnull Collection<? extends Measurement> measurements) {
    super(measurements, Layer1RelativeMedium.SINGLE_LAYER);
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

