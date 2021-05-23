package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Metrics;
import com.ak.util.Strings;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;

final class TetrapolarPrediction implements Prediction {
  @Nonnegative
  private final double resistivityPredicted;
  @Nonnull
  private final double[] horizons;

  @ParametersAreNonnullByDefault
  TetrapolarPrediction(InexactTetrapolarSystem system, RelativeMediumLayers<Double> layers, @Nonnegative double rho1) {
    double predicted = rho1;

    if (Double.compare(layers.k12(), 0.0) != 0) {
      predicted = new NormalizedApparent2Rho(system.toExact().toRelative()).value(layers.k12(), layers.hToL()) * rho1;
    }

    resistivityPredicted = predicted;
    horizons = new double[] {system.getHMin(layers.k12()), system.getHMax(layers.k12())};
  }

  @Override
  public double getResistivityPredicted() {
    return resistivityPredicted;
  }

  @Override
  public double[] getHorizons() {
    return Arrays.copyOf(horizons, horizons.length);
  }

  @Override
  public String toString() {
    return "predicted %s; %s".formatted(Strings.rho(resistivityPredicted), toStringHorizons(horizons));
  }

  @Nonnull
  static String toStringHorizons(@Nonnull double[] horizons) {
    return Arrays.stream(horizons)
        .map(Metrics::toMilli).mapToObj("%.1f"::formatted)
        .collect(Collectors.joining("; ", "\u2194 [", "] " + MetricPrefix.MILLI(METRE)));
  }

  @Nonnull
  static double[] mergeHorizons(@Nonnull Collection<Prediction> predictions) {
    return predictions.stream().map(Prediction::getHorizons).collect(
        Collectors.teeing(
            Collectors.maxBy(Comparator.comparingDouble(value -> value[0])),
            Collectors.minBy(Comparator.comparingDouble(value -> value[1])),
            (doubles1, doubles2) -> Optional.of(
                new double[] {
                    Math.max(doubles1.orElseThrow()[0], doubles2.orElseThrow()[0]),
                    Math.min(doubles1.orElseThrow()[1], doubles2.orElseThrow()[1])
                }
            )
        )
    ).orElseThrow();
  }
}
