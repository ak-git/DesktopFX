package com.ak.rsm.medium;

import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.prediction.Prediction;
import com.ak.rsm.prediction.Predictions;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import tec.uom.se.unit.MetricPrefix;

import javax.annotation.Nonnegative;
import java.util.*;
import java.util.stream.Collectors;

import static tec.uom.se.unit.Units.METRE;

public final class Layer2Medium extends AbstractMediumLayers {
  private final RelativeMediumLayers kw;
  private final Layer1Medium layer1;
  private final ValuePair rho1;
  private final double dRho2;
  @Nonnegative
  private final double baseL;

  public Layer2Medium(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    super(measurements);
    this.kw = Objects.requireNonNull(kw);
    baseL = Resistivity.getBaseL(measurements);
    layer1 = new Layer1Medium(measurements);
    rho1 = getRho1();
    dRho2 = 2.0 * kw.k().absError() / StrictMath.pow(1.0 - kw.k().value(), 2.0) +
        (rho1().absError() / Layers.getRho1ToRho2(kw.k().value()));
  }

  @Override
  public ValuePair rho() {
    return layer1.rho();
  }

  public ValuePair rho1() {
    return rho1;
  }

  public ValuePair rho2() {
    return ValuePair.Name.RHO_2.of(rho1().value() / Layers.getRho1ToRho2(kw.k().value()), dRho2);
  }

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
  public Prediction apply(Measurement measurement) {
    return Predictions.of(measurement, kw, rho1.value());
  }

  private static String toStringHorizons(double[] horizons) {
    return Arrays.stream(Objects.requireNonNull(horizons))
        .map(metre -> Metrics.Length.METRE.to(metre, MetricPrefix.MILLI(METRE))).mapToObj("%.1f"::formatted)
        .collect(Collectors.joining("; ", "↔ [", "] " + MetricPrefix.MILLI(METRE)));
  }

  private static double[] mergeHorizons(Collection<Measurement> measurements, double k) {
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

  private ValuePair getRho1() {
    if (RelativeMediumLayers.SINGLE_LAYER.equals(kw)) {
      ValuePair rho = rho();
      return ValuePair.Name.RHO_1.of(rho.value(), rho.absError());
    }
    else if (RelativeMediumLayers.NAN.equals(kw)) {
      return ValuePair.Name.RHO_1.of(Double.NaN, Double.NaN);
    }
    return measurements().stream()
        .<ValuePair>mapMulti((measurement, consumer) -> {
          TetrapolarSystem s = measurement.system();
          RelativeMediumLayers layer2RelativeMedium = new RelativeMediumLayers(kw.k().value(), kw.hToL().value() * baseL / s.lCC());

          double normApparent = Apparent2Rho.newApparentDivRho1(s.relativeSystem()).applyAsDouble(layer2RelativeMedium);
          double fK = Math.abs(Apparent2Rho.newDerApparentByKDivRho1(s.relativeSystem()).applyAsDouble(kw) * kw.k().absError());
          double fPhi = Math.abs(Apparent2Rho.newDerApparentByPhiDivRho1(s.relativeSystem()).applyAsDouble(kw) * kw.hToL().absError());
          double rho1Value = measurement.resistivity() / normApparent;
          consumer.accept(ValuePair.Name.RHO_1.of(rho1Value, ((fK + fPhi) / normApparent) * rho1Value));

          if (measurement instanceof TetrapolarDerivativeMeasurement dm && !Double.isNaN(dm.derivativeResistivity())) {
            double normDer = Apparent2Rho.newDerApparentByPhiDivRho1(s.relativeSystem()).applyAsDouble(layer2RelativeMedium);
            double fKDer = Math.abs(Apparent2Rho.newSecondDerApparentByPhiKDivRho1(s.relativeSystem()).applyAsDouble(kw) * kw.k().absError());
            double fPhiDer = Math.abs(Apparent2Rho.newSecondDerApparentByPhiPhiDivRho1(s.relativeSystem()).applyAsDouble(kw) * kw.hToL().absError());
            double rho1Der = dm.derivativeResistivity() / normDer;
            consumer.accept(ValuePair.Name.RHO_1.of(rho1Der, ((fKDer + fPhiDer) / normDer) * rho1Der));
          }
        })
        .reduce(ValuePair::mergeWith).orElseThrow();
  }
}

