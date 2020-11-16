package com.ak.rsm;

import java.util.Collection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Strings;

final class Layer1Medium extends AbstractMediumLayers<Layer1Medium> {
  private Layer1Medium(@Nonnull Layer1MediumBuilder builder) {
    super(builder);
  }

  @Override
  public String toString() {
    return "%s; %s".formatted(Strings.rho(rho()), super.toString());
  }

  static final class Layer1MediumBuilder extends AbstractMediumBuilder<Layer1Medium> {
    Layer1MediumBuilder(@Nonnull Collection<Prediction> predictions) {
      super(predictions);
    }

    Layer1MediumBuilder layer1(@Nonnegative double rho) {
      this.rho = rho;
      return this;
    }

    @Override
    public Layer1Medium build() {
      return new Layer1Medium(this);
    }
  }
}
