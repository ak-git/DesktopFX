package com.ak.rsm.prediction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.ak.util.Metrics;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;

abstract class AbstractPrediction implements Prediction {
  @Nonnegative
  private final double resistivityPredicted;
  @Nonnull
  private final double[] inequalityL2;

  protected AbstractPrediction(@Nonnegative double resistivityPredicted, @Nonnull double[] inequalityL2) {
    this.resistivityPredicted = resistivityPredicted;
    this.inequalityL2 = Arrays.copyOf(inequalityL2, inequalityL2.length);
  }

  @Override
  public final double getResistivityPredicted() {
    return resistivityPredicted;
  }

  @Override
  public final double[] getInequalityL2() {
    return Arrays.copyOf(inequalityL2, inequalityL2.length);
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public boolean equals(Object o) {
    if (!(o instanceof AbstractPrediction that)) {
      return false;
    }
    return Double.compare(that.resistivityPredicted, resistivityPredicted) == 0 && Arrays.equals(inequalityL2, that.inequalityL2);
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public int hashCode() {
    int result = Objects.hash(resistivityPredicted);
    result = 31 * result + Arrays.hashCode(inequalityL2);
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
