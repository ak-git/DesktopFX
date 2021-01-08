package com.ak.rsm;

import java.util.Collection;

import javax.annotation.Nonnull;

final class Layer1Medium extends AbstractMediumLayers<Layer1Medium> {
  @Nonnull
  private final Measurement measurement;

  private Layer1Medium(@Nonnull Layer1MediumBuilder builder) {
    super(builder);
    measurement = builder.measurement;
  }

  @Override
  public String toString() {
    return "%s; %s".formatted(measurement, super.toString());
  }

  static final class Layer1MediumBuilder extends AbstractMediumBuilder<Layer1Medium> {
    private Measurement measurement;

    Layer1MediumBuilder(@Nonnull Collection<Prediction> predictions) {
      super(predictions);
    }

    Layer1MediumBuilder layer1(@Nonnull Measurement measurement) {
      this.measurement = measurement;
      rho = measurement.getResistivity();
      return this;
    }

    @Override
    public Layer1Medium build() {
      return new Layer1Medium(this);
    }
  }
}
