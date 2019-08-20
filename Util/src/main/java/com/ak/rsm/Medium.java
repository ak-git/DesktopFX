package com.ak.rsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import tec.uom.se.unit.Units;

class Medium {
  @Nonnull
  private final double[] measured;
  @Nonnull
  private final double[] predicted;
  @Nonnull
  private final double[] rho;
  @Nonnull
  private final double[] h;

  private Medium(Builder b) {
    measured = b.measured;
    predicted = b.predicted;
    rho = DoubleStream.concat(b.layers.stream().mapToDouble(value -> value[0]), DoubleStream.of(b.rhoSemiInfinite)).toArray();
    h = b.layers.stream().mapToDouble(value -> value[1]).toArray();
  }

  double getRho() {
    if (rho.length == 1) {
      return rho[0];
    }
    else {
      throw new UnsupportedOperationException(Arrays.toString(rho));
    }
  }

  double getRho1() {
    return rho[0];
  }

  double getRho2() {
    return rho[1];
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (rho.length == 1) {
      sb.append(Strings.rho(rho[0]));
    }
    else {
      sb.append(IntStream.range(0, rho.length).mapToObj(i -> Strings.rho(rho[i], i + 1)).collect(Collectors.joining(", ")));
      sb.append("; ");
      sb.append(IntStream.range(0, h.length).mapToObj(i -> Strings.h(h[i], i + 1)).collect(Collectors.joining(", ")));
    }

    return String.format("%s; measured = %s, predicted = %s; L%s = %.1f %s", sb.toString(),
        Strings.toString("%.3f", measured, Units.OHM),
        Strings.toString("%.3f", predicted, Units.OHM),
        Strings.low(2),
        Metrics.toPercents(Inequality.proportional().applyAsDouble(measured, predicted) / measured.length),
        Units.PERCENT
    );
  }

  static class Builder {
    @Nonnull
    private final double[] measured;
    @Nonnull
    private final double[] predicted;
    private final Collection<double[]> layers = new ArrayList<>();
    @Nonnegative
    private double rhoSemiInfinite = Double.POSITIVE_INFINITY;

    Builder(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms, @Nonnull ToDoubleFunction<? super TetrapolarSystem> toDoubleFunction) {
      measured = Arrays.copyOf(rOhms, rOhms.length);
      predicted = Arrays.stream(systems).mapToDouble(toDoubleFunction).toArray();
    }

    Builder addLayer(@Nonnegative double rho, @Nonnegative double h) {
      layers.add(new double[] {rho, h});
      return this;
    }

    public Medium build(@Nonnegative double rhoSemiInfinite) {
      this.rhoSemiInfinite = rhoSemiInfinite;
      return new Medium(this);
    }
  }
}
