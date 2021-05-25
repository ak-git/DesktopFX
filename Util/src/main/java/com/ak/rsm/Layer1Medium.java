package com.ak.rsm;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.ak.math.ValuePair;
import com.ak.util.Strings;

final class Layer1Medium extends AbstractMediumLayers {
  Layer1Medium(@Nonnull Collection<? extends Measurement> measurements) {
    super(measurements, RelativeMediumLayers.SINGLE_LAYER);
  }

  @Override
  public ValuePair h1() {
    return new ValuePair(Double.NaN);
  }

  @Override
  public String toString() {
    return "%s; %s".formatted(Strings.rho(1, rho()), super.toString());
  }
}
