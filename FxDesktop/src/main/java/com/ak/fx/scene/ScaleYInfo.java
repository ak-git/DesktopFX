package com.ak.fx.scene;

import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;

public final class ScaleYInfo<EV extends Enum<EV> & Variable<EV>> implements IntToDoubleFunction, Runnable, DoubleFunction<String> {
  private final EV variable;
  private final Consumer<ScaleYInfo<EV>> scaledConsumer;
  private final int mean;
  private final int scaleFactor;
  private final int scaleFactor10;

  ScaleYInfo(@Nonnull Builder<EV> builder) {
    variable = builder.variable;
    scaledConsumer = builder.scaledConsumer;
    mean = builder.mean;
    scaleFactor = builder.scaleFactor;
    scaleFactor10 = builder.scaleFactor10;
  }

  @Override
  public double applyAsDouble(int value) {
    return GridCell.mmToScreen(value - mean) / scaleFactor;
  }

  @Override
  public void run() {
    scaledConsumer.accept(this);
  }

  @Override
  public String apply(double fromMean) {
    return Variables.toString(mean + GridCell.mm(scaleFactor * fromMean), variable.getUnit(), scaleFactor10);
  }

  @Override
  public String toString() {
    return String.format("ScaleYInfo{mean = %d, scaleFactor = %d, scaleFactor10 = %d}", mean, scaleFactor, scaleFactor10);
  }

  static final class Builder<EV extends Enum<EV> & Variable<EV>> implements javafx.util.Builder<ScaleYInfo<EV>> {
    private final EV variable;
    private final Consumer<ScaleYInfo<EV>> scaledConsumer;
    private int mean;
    private int scaleFactor = 1;
    private int scaleFactor10 = 1;

    Builder(@Nonnull EV variable, @Nonnull Consumer<ScaleYInfo<EV>> scaledConsumer) {
      this.variable = variable;
      this.scaledConsumer = scaledConsumer;
    }

    Builder<EV> mean(int mean) {
      this.mean = mean;
      return this;
    }

    Builder<EV> scaleFactor(@Nonnegative int scaleFactor) {
      this.scaleFactor = scaleFactor;
      return this;
    }

    Builder<EV> scaleFactor10(@Nonnegative int scaleFactor10) {
      this.scaleFactor10 = scaleFactor10;
      return this;
    }

    @Override
    public ScaleYInfo<EV> build() {
      return new ScaleYInfo<>(this);
    }
  }
}

