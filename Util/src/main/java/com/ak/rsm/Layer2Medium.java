package com.ak.rsm;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Strings;

final class Layer2Medium<D> extends AbstractMediumLayers<D, Layer2Medium<D>> {
  @Nonnull
  private final D h1;
  @Nonnull
  private final D rho2;

  private Layer2Medium(@Nonnull Layer2MediumBuilder<D> builder) {
    super(builder);
    h1 = builder.h1;
    rho2 = builder.rho2;
  }

  @Override
  public D h1() {
    return h1;
  }

  @Override
  public D rho2() {
    return rho2;
  }

  @Override
  public String toString() {
    return "%s; %s; h = %s; %s".formatted(Strings.rho(1, rho1()), Strings.rho(2, rho2()), h1, super.toString());
  }

  static final class Layer2MediumBuilder<D> extends AbstractMediumBuilder<D, Layer2Medium<D>> {
    D rho2;
    D h1;

    Layer2MediumBuilder(@Nonnull Collection<Prediction> predictions) {
      super(predictions);
    }

    @ParametersAreNonnullByDefault
    Layer2MediumBuilder<D> layer1(D rho1, D h1) {
      rho = rho1;
      this.h1 = h1;
      return this;
    }

    Layer2MediumBuilder<D> layer2(@Nonnull D rho2) {
      this.rho2 = rho2;
      return this;
    }

    @Override
    public Layer2Medium<D> build() {
      return new Layer2Medium<>(this);
    }
  }
}
