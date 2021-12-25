package com.ak.rsm.prediction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.medium.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;

public record TetrapolarPrediction(@Nonnegative double resistivityPredicted, @Nonnull double[] horizons,
                                   @Nonnegative double inequalityL2) implements Prediction {
  @ParametersAreNonnullByDefault
  public static Prediction of(InexactTetrapolarSystem inexact, RelativeMediumLayers layers,
                              @Nonnegative double rho1, @Nonnegative double resistivityMeasured) {
    double resistivityPredicted;
    if (Double.compare(layers.k12(), 0.0) == 0) {
      resistivityPredicted = rho1;
    }
    else {
      resistivityPredicted = Apparent2Rho.newNormalizedApparent2Rho(inexact.system().relativeSystem()).applyAsDouble(layers) * rho1;
    }
    double[] horizons = {inexact.getHMin(layers.k12()), inexact.getHMax(layers.k12())};
    double inequalityL2 = Inequality.proportional().applyAsDouble(resistivityMeasured, resistivityPredicted);
    return new TetrapolarPrediction(resistivityPredicted, horizons, inequalityL2);
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
  public double[] getInequalityL2() {
    return new double[] {inequalityL2};
  }

  @Override
  public String toString() {
    return "predicted %s; %s".formatted(Strings.rho(resistivityPredicted), toStringHorizons(horizons));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !getClass().equals(o.getClass())) {
      return false;
    }
    TetrapolarPrediction that = (TetrapolarPrediction) o;
    return Double.compare(that.resistivityPredicted, resistivityPredicted) == 0 &&
        Double.compare(that.inequalityL2, inequalityL2) == 0 &&
        Arrays.equals(horizons, that.horizons);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(resistivityPredicted, inequalityL2);
    result = 31 * result + Arrays.hashCode(horizons);
    return result;
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
