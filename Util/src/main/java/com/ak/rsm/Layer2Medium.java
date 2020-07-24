package com.ak.rsm;

import java.util.Collection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Strings;

final class Layer2Medium extends AbstractMediumLayers<Layer2Medium> {
  @Nonnegative
  private final double rho2;
  @Nonnull
  private final Layer2RelativeMedium layer2RelativeMedium;

  private Layer2Medium(@Nonnull Layer2MediumBuilder builder) {
    super(builder);
    rho2 = builder.rho2;
    layer2RelativeMedium = new Layer2RelativeMedium(Layers.getK12(builder.rho, rho2), builder.h);
  }

  @Override
  public double rho2() {
    return rho2;
  }

  @Override
  public double h() {
    return layer2RelativeMedium.h();
  }

  @Override
  public String toString() {
    return String.format("%s; %s; %s; %s", Strings.rho1(rho1()), Strings.rho2(rho2()), layer2RelativeMedium, super.toString());
  }

  static final class Layer2MediumBuilder extends AbstractMediumBuilder<Layer2Medium> {
    @Nonnegative
    private double rho2;
    @Nonnegative
    private double h;

    Layer2MediumBuilder(@Nonnull Collection<Prediction> predictions) {
      super(predictions);
    }

    Layer2MediumBuilder layer1(@Nonnegative double rho1, @Nonnegative double h) {
      rho = rho1;
      this.h = h;
      return this;
    }

    Layer2MediumBuilder layer2(@Nonnegative double rho2) {
      this.rho2 = rho2 > 1000.0 ? Double.POSITIVE_INFINITY : rho2;
      return this;
    }

    @Override
    public Layer2Medium build() {
      return new Layer2Medium(this);
    }
  }
}
