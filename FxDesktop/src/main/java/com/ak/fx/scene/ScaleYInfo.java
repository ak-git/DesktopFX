package com.ak.fx.scene;

import java.util.function.DoubleFunction;
import java.util.function.IntToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;

public final class ScaleYInfo<V extends Enum<V> & Variable<V>> implements IntToDoubleFunction, DoubleFunction<String> {
  private final V variable;
  private final int mean;
  private final int scaleFactor;
  private final int scaleFactor10;

  private ScaleYInfo(@Nonnull ScaleYInfoBuilder<V> builder) {
    variable = builder.variable;
    mean = builder.mean;
    if (variable.options().contains(Variable.Option.INVERSE)) {
      scaleFactor = -builder.scaleFactor;
    }
    else {
      scaleFactor = builder.scaleFactor;
    }
    scaleFactor10 = builder.scaleFactor10;
  }

  @Override
  public double applyAsDouble(int value) {
    return GridCell.mmToScreen(value - mean) / scaleFactor;
  }

  @Override
  public String apply(double fromMean) {
    return Variables.toString(mean + GridCell.mm(scaleFactor * fromMean), variable.getUnit(), scaleFactor10);
  }

  @Override
  public String toString() {
    return String.format("ScaleYInfo{mean = %d, scaleFactor = %d, scaleFactor10 = %d}", mean, scaleFactor, scaleFactor10);
  }

  static final class ScaleYInfoBuilder<V extends Enum<V> & Variable<V>> implements javafx.util.Builder<ScaleYInfo<V>> {
    private final V variable;
    private int mean;
    private int scaleFactor = 1;
    private int scaleFactor10 = 1;

    ScaleYInfoBuilder(@Nonnull V variable) {
      this.variable = variable;
    }

    ScaleYInfoBuilder<V> mean(int mean) {
      this.mean = mean;
      return this;
    }

    ScaleYInfoBuilder<V> scaleFactor(@Nonnegative int scaleFactor) {
      this.scaleFactor = scaleFactor;
      return this;
    }

    ScaleYInfoBuilder<V> scaleFactor10(@Nonnegative int scaleFactor10) {
      this.scaleFactor10 = scaleFactor10;
      return this;
    }

    @Override
    public ScaleYInfo<V> build() {
      return new ScaleYInfo<>(this);
    }
  }
}

