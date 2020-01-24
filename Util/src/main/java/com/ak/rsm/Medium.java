package com.ak.rsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

class Medium {
  @Nonnull
  private final double[] measured;
  @Nonnull
  private final double[] predicted;
  @Nonnull
  private final double[] measuredDelta;
  @Nonnull
  private final double[] predictedDelta;
  @Nonnull
  private final double[] rho;
  @Nonnull
  private final double[] h;

  private Medium(Builder b) {
    measured = b.measured;
    predicted = b.predicted;
    measuredDelta = b.measuredDelta;
    predictedDelta = b.predictedDelta;
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

  @Nonnull
  public double[] getH() {
    return Arrays.copyOf(h, h.length);
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

    if (measuredDelta.length == 0 || predictedDelta.length == 0) {
      return String.format("%s; L%s = %.1f %s;%nmeasured  = %s;%npredicted = %s;", sb,
          Strings.low(2),
          Metrics.toPercents(Inequality.proportional().applyAsDouble(measured, predicted) / measured.length),
          Units.PERCENT,
          Strings.toString("%.3f", measured, Units.OHM),
          Strings.toString("%.3f", predicted, Units.OHM)
      );
    }
    else {
      double error = Inequality.proportional().applyAsDouble(measured, predicted) / measured.length;
      double error2 = Inequality.proportional().applyAsDouble(measuredDelta, predictedDelta) / measuredDelta.length;

      return String.format("%s; L%s = [%.1f; %.1f] %s;%nmeasured  = %s, %s = %s;%npredicted = %s, %s = %s;", sb,
          Strings.low(2),
          Metrics.toPercents(error),
          Metrics.toPercents(error2),
          Units.PERCENT,
          Strings.toString("%.3f", measured, Units.OHM),
          Strings.CAP_DELTA,
          Strings.toString("%.0f", Arrays.stream(measuredDelta).map(Metrics::toMilli).toArray(), MetricPrefix.MILLI(Units.OHM)),
          Strings.toString("%.3f", predicted, Units.OHM),
          Strings.CAP_DELTA,
          Strings.toString("%.0f", Arrays.stream(predictedDelta).map(Metrics::toMilli).toArray(), MetricPrefix.MILLI(Units.OHM))
      );
    }
  }

  static class Builder {
    public static final double[] EMPTY = {};
    @Nonnull
    private final double[] measured;
    @Nonnull
    private final double[] predicted;
    @Nonnull
    private final double[] measuredDelta;
    @Nonnull
    private final double[] predictedDelta;
    @Nonnull
    private final Collection<double[]> layers = new ArrayList<>();
    @Nonnegative
    private double rhoSemiInfinite = Double.POSITIVE_INFINITY;

    Builder(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms, @Nonnull ToDoubleFunction<? super TetrapolarSystem> toDoubleFunction) {
      measured = Arrays.copyOf(rOhms, rOhms.length);
      predicted = Arrays.stream(systems).mapToDouble(toDoubleFunction).toArray();
      measuredDelta = EMPTY;
      predictedDelta = EMPTY;
    }

    Builder(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter,
            double dh, @Nonnull ToDoubleBiFunction<TetrapolarSystem, Double> toOhms) {
      measured = Arrays.copyOf(rOhmsBefore, rOhmsBefore.length);
      predicted = Arrays.stream(systems).mapToDouble(system -> toOhms.applyAsDouble(system, 0.0)).toArray();

      measuredDelta = IntStream.range(0, rOhmsBefore.length).mapToDouble(i -> rOhmsAfter[i] - rOhmsBefore[i]).toArray();
      predictedDelta = Arrays.stream(systems).mapToDouble(system -> toOhms.applyAsDouble(system, dh) - toOhms.applyAsDouble(system, 0.0)).toArray();
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
