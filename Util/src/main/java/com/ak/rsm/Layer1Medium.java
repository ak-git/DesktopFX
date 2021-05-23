package com.ak.rsm;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.ak.math.ValuePair;
import com.ak.util.Strings;

final class Layer1Medium extends AbstractMediumLayers<ValuePair, Layer1Medium> {
  @Nonnull
  private final Measurement measurement;

  private Layer1Medium(@Nonnull Layer1MediumBuilder builder) {
    super(builder);
    measurement = builder.measurement;
  }

  @Override
  public ValuePair h1() {
    return new ValuePair(Double.NaN);
  }

  @Override
  public String toString() {
    return "%s; %s; %s".formatted(Strings.rho(1, rho()), measurement, super.toString());
  }

  static final class Layer1MediumBuilder extends AbstractMediumBuilder<ValuePair, Layer1Medium> {
    private Measurement measurement;

    Layer1MediumBuilder(@Nonnull Collection<Prediction> predictions) {
      super(predictions);
    }

    Layer1MediumBuilder layer1(@Nonnull Measurement measurement) {
      this.measurement = measurement;
      rho = new ValuePair(
          measurement.getResistivity(),
          measurement.getResistivity() * measurement.getSystem().getApparentRelativeError()
      );
      return this;
    }

    @Override
    public Layer1Medium build() {
      return new Layer1Medium(this);
    }
  }
}
