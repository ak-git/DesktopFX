package com.ak.rsm;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;

final class TetrapolarPrediction implements Prediction {
  private static final double[] EMPTY = {};
  @Nonnull
  private final Measurement measurement;
  @Nonnegative
  private final double resistivityPredicted;
  @Nonnull
  private final double[] horizons;

  TetrapolarPrediction(@Nonnull Measurement measurement, @Nonnegative double resistivityPredicted) {
    this.measurement = measurement;
    this.resistivityPredicted = resistivityPredicted;
    horizons = EMPTY;
  }

  @ParametersAreNonnullByDefault
  TetrapolarPrediction(Measurement measurement, RelativeMediumLayers layers, @Nonnegative double rho1) {
    double resistivityPredicted = rho1;

    InexactTetrapolarSystem inexact = measurement.getSystem();
    if (Double.compare(layers.k12(), 0.0) != 0) {
      TetrapolarSystem system = inexact.toExact();
      resistivityPredicted = new NormalizedApparent2Rho(system.toRelative())
          .value(layers.k12(), layers.h() / system.getL()) * rho1;
    }

    this.measurement = measurement;
    this.resistivityPredicted = resistivityPredicted;
    horizons = new double[] {inexact.getHMin(layers.k12()), inexact.getHMax(layers.k12())};
  }

  @Override
  public double getInequalityL2() {
    return Inequality.proportional().applyAsDouble(measurement.getResistivity(), resistivityPredicted);
  }

  @Override
  public double getResistivityPredicted() {
    return resistivityPredicted;
  }

  @Override
  public String toString() {
    return "%s; predicted %s; %s".formatted(String.valueOf(measurement), Strings.rho(resistivityPredicted),
        Arrays.stream(horizons)
            .map(Metrics::toMilli).mapToObj("%.1f"::formatted)
            .collect(Collectors.joining("; ", "\u2194 [", "] " + MetricPrefix.MILLI(METRE)))
    );
  }
}
