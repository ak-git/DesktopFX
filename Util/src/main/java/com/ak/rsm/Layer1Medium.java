package com.ak.rsm;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.ak.math.ValuePair;
import com.ak.util.Strings;

final class Layer1Medium extends AbstractMediumLayers<Layer1Medium> {
  @Nonnull
  private final ValuePair rho;

  Layer1Medium(@Nonnull Collection<Measurement> measurements) {
    super(measurements, measurement -> measurement.toPrediction(RelativeMediumLayers.SINGLE_LAYER, getRho(measurements).getValue()));
    rho = getRho(measurements);
  }

  @Nonnull
  @Override
  public ValuePair rho() {
    return rho;
  }

  @Override
  public ValuePair h1() {
    return new ValuePair(Double.NaN);
  }

  @Override
  public String toString() {
    return "%s; %s".formatted(Strings.rho(1, rho()), super.toString());
  }

  @Nonnull
  private static ValuePair getRho(@Nonnull Collection<Measurement> measurements) {
    Measurement average = measurements.stream().reduce(Measurement::merge).orElseThrow();
    double rho = average.getResistivity();
    return new ValuePair(rho, rho * average.getSystem().getApparentRelativeError());
  }
}
