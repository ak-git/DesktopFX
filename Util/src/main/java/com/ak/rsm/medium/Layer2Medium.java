package com.ak.rsm.medium;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.prediction.Prediction;
import com.ak.rsm.prediction.Predictions;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.Layers;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import tec.uom.se.unit.MetricPrefix;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import static tec.uom.se.unit.Units.METRE;

public final class Layer2Medium extends AbstractMediumLayers {
  @Nonnull
  private final RelativeMediumLayers kw;
  @Nonnull
  private final Layer1Medium layer1;
  @Nonnull
  private final ValuePair rho1;
  private final double dRho2;
  @Nonnegative
  private final double baseL;

  @ParametersAreNonnullByDefault
  public Layer2Medium(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    super(measurements);
    this.kw = kw;
    layer1 = new Layer1Medium(measurements);
    rho1 = Measurements.getRho1(measurements, kw);
    dRho2 = 2.0 * kw.k().absError() / StrictMath.pow(1.0 - kw.k().value(), 2.0) +
        (rho1().absError() / Layers.getRho1ToRho2(kw.k().value()));
    baseL = Resistivity.getBaseL(measurements);
  }

  @Nonnull
  @Override
  public ValuePair rho() {
    return layer1.rho();
  }

  @Nonnull
  public ValuePair rho1() {
    return rho1;
  }

  @Nonnull
  public ValuePair rho2() {
    return ValuePair.Name.RHO_2.of(rho1().value() / Layers.getRho1ToRho2(kw.k().value()), dRho2);
  }

  @Nonnull
  public ValuePair h() {
    return ValuePair.Name.H.of(kw.hToL().value() * baseL, kw.hToL().absError() * baseL);
  }

  @Override
  public String toString() {
    if (kw.size() == 1) {
      return layer1.toString();
    }

    double k = kw.k().value();
    return "%s; %s; %s; %s; %s; %s; %s %n%s".formatted(rho(), rho1, rho2(), h(), kw,
        toStringHorizons(mergeHorizons(measurements(), k)), toStringRMS(),
        measurements().stream()
            .map(m ->
                "%s; %s; %s".formatted(m, apply(m), toStringHorizons(
                    new double[] {m.inexact().getHMin(k), m.inexact().getHMax(k)}
                ))
            )
            .collect(Collectors.joining(Strings.NEW_LINE))
    );
  }

  @Override
  @Nonnull
  public Prediction apply(@Nonnull Measurement measurement) {
    return Predictions.of(measurement, kw, rho1.value());
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

