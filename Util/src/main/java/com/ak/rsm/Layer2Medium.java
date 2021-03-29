package com.ak.rsm;

import java.util.Collection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.util.Strings;

final class Layer2Medium<D> extends AbstractMediumLayers<D, Layer2Medium<D>> {
  @Nonnull
  private final D rho2;
  @Nonnull
  private final Layer2RelativeMedium<D> layer2RelativeMedium;

  private Layer2Medium(@Nonnull AbstractLayer2MediumBuilder<D> builder) {
    super(builder);
    rho2 = builder.rho2;
    layer2RelativeMedium = new Layer2RelativeMedium<>(builder.k12, builder.h);
  }

  @Override
  public D rho2() {
    return rho2;
  }

  @Override
  public D k12() {
    return layer2RelativeMedium.k12();
  }

  @Override
  public D h() {
    return layer2RelativeMedium.h();
  }

  @Override
  public String toString() {
    return "%s; %s; %s; %s".formatted(Strings.rho(rho1(), 1), Strings.rho(rho2(), 2), layer2RelativeMedium, super.toString());
  }

  abstract static class AbstractLayer2MediumBuilder<D> extends AbstractMediumBuilder<D, Layer2Medium<D>> {
    D rho2;
    D h;
    D k12;

    AbstractLayer2MediumBuilder(@Nonnull Collection<Prediction> predictions) {
      super(predictions);
    }
  }

  static final class DoubleLayer2MediumBuilder extends AbstractLayer2MediumBuilder<Double> {
    DoubleLayer2MediumBuilder(@Nonnull Collection<Prediction> predictions) {
      super(predictions);
    }

    DoubleLayer2MediumBuilder layer1(@Nonnegative double rho1, @Nonnegative double h) {
      rho = rho1;
      this.h = h;
      return this;
    }

    DoubleLayer2MediumBuilder layer2(@Nonnegative double rho2) {
      this.rho2 = Layers.getK12(rho, rho2) > 0.99 ? Double.POSITIVE_INFINITY : rho2;
      return this;
    }

    @Override
    public Layer2Medium<Double> build() {
      k12 = Layers.getK12(rho, rho2);
      return new Layer2Medium<>(this);
    }
  }

  static final class Layer2MediumBuilder extends AbstractLayer2MediumBuilder<ValuePair> {
    Layer2MediumBuilder(@Nonnull Collection<Prediction> predictions) {
      super(predictions);
    }

    @ParametersAreNonnullByDefault
    Layer2MediumBuilder layer1(ValuePair rho1, ValuePair h) {
      rho = rho1;
      this.h = h;
      return this;
    }

    Layer2MediumBuilder layer2(@Nonnull ValuePair rho2) {
      this.rho2 = rho2;
      return this;
    }

    Layer2MediumBuilder k12(@Nonnull ValuePair k12) {
      this.k12 = k12;
      return this;
    }

    @Override
    public Layer2Medium<ValuePair> build() {
      return new Layer2Medium<>(this);
    }
  }
}
