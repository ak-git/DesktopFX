package com.ak.rsm.medium;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.prediction.Prediction;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import tec.uom.se.unit.MetricPrefix;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

import static com.ak.rsm.measurement.Measurements.getRho1;
import static tec.uom.se.unit.Units.METRE;

abstract sealed class AbstractMediumLayers implements MediumLayers permits Layer1Medium, Layer2Medium {
  @Nonnull
  private final RelativeMediumLayers kw;
  @Nonnull
  private final ValuePair rho;
  @Nonnegative
  private final double baseL;
  @Nonnull
  private final Collection<Measurement> measurements;
  @Nonnull
  private final Collection<Prediction> predictions;

  @ParametersAreNonnullByDefault
  AbstractMediumLayers(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    this.kw = kw;
    rho = getRho1(measurements, kw);
    this.measurements = Collections.unmodifiableCollection(measurements);
    baseL = Measurements.getBaseL(measurements);
    predictions = measurements.stream()
        .map(m ->
            m.toPrediction(
                new Layer2RelativeMedium(kw.k12(), kw.hToL() * baseL / m.system().lCC()),
                rho.value()
            )
        )
        .toList();
  }

  @Override
  public final ValuePair rho() {
    return rho;
  }

  @Nonnull
  final RelativeMediumLayers kw() {
    return kw;
  }

  @Nonnegative
  final double baseL() {
    return baseL;
  }

  @Override
  public final double[] getRMS() {
    double[] l2 = predictions.stream().map(Prediction::getInequalityL2)
        .reduce((doubles, doubles2) -> {
          var merge = new double[Math.max(doubles.length, doubles2.length)];
          for (var i = 0; i < merge.length; i++) {
            merge[i] = StrictMath.hypot(doubles[i], doubles2[i]);
          }
          return merge;
        }).orElseThrow();
    return Arrays.stream(l2).map(operand -> operand / Math.sqrt(predictions.size())).toArray();
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public String toString() {
    var data = new StringJoiner(Strings.EMPTY);
    Iterator<Measurement> mIterator = measurements.iterator();
    Iterator<Prediction> pIterator = predictions.iterator();
    while (mIterator.hasNext() && pIterator.hasNext()) {
      Measurement m = mIterator.next();
      data.add(m.toString());
      if (!Double.isNaN(rho1().value())) {
        data.add("; %s; %s".formatted(pIterator.next(), toStringHorizons(new double[] {
            m.inexact().getHMin(kw.k12()), m.inexact().getHMax(kw.k12())
        })));
      }
      data.add(Strings.NEW_LINE);
    }

    StringJoiner joiner = new StringJoiner("; ");
    if (!Double.isNaN(rho1().value())) {
      if (!kw.toString().isEmpty()) {
        joiner.add(kw.toString());
      }
      joiner.add(toStringHorizons(mergeHorizons(measurements, kw.k12())))
          .add("RMS = %s %%".formatted(
                  Arrays.stream(getRMS())
                      .map(Metrics::toPercents).mapToObj("%.1f"::formatted)
                      .collect(Collectors.joining("; ", "[", "]"))
              )
          );
    }
    return joiner.add("%n%s".formatted(data.toString())).toString();
  }

  @Nonnull
  private static String toStringHorizons(@Nonnull double[] horizons) {
    return Arrays.stream(horizons)
        .map(Metrics::toMilli).mapToObj("%.1f"::formatted)
        .collect(Collectors.joining("; ", "↔ [", "] " + MetricPrefix.MILLI(METRE)));
  }

  @Nonnull
  private static double[] mergeHorizons(@Nonnull Collection<Measurement> measurements, double k) {
    return measurements.stream().map(Measurement::inexact).collect(
        Collectors.teeing(
            Collectors.maxBy(Comparator.comparingDouble(v -> v.getHMin(k))),
            Collectors.minBy(Comparator.comparingDouble(v -> v.getHMax(k))),
            (vs1, vs2) -> Optional.of(
                new double[] {
                    Math.max(vs1.orElseThrow().getHMin(k), vs2.orElseThrow().getHMin(k)),
                    Math.min(vs1.orElseThrow().getHMax(k), vs2.orElseThrow().getHMax(k))
                }
            )
        )
    ).orElseThrow();
  }
}

