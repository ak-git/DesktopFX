package com.ak.rsm.medium;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.prediction.Prediction;
import com.ak.util.Metrics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

import static tec.uom.se.unit.Units.PERCENT;

abstract sealed class AbstractMediumLayers implements MediumLayers, Function<Measurement, Prediction>
    permits Layer1Medium, Layer2Medium {
  private final Collection<Measurement> measurements;

  AbstractMediumLayers(Collection<? extends Measurement> measurements) {
    if (measurements.isEmpty()) {
      throw new IllegalArgumentException("Empty measurements");
    }
    this.measurements = Collections.unmodifiableCollection(measurements);
  }

  @Override
  public final double[] getRMS() {
    double[] l2 = measurements().stream().map(this).map(Prediction::getInequalityL2)
        .reduce((doubles, doubles2) -> {
          var merge = new double[Math.max(doubles.length, doubles2.length)];
          for (var i = 0; i < merge.length; i++) {
            merge[i] = StrictMath.hypot(doubles[i], doubles2[i]);
          }
          return merge;
        }).orElseThrow();
    return Arrays.stream(l2).map(operand -> operand / Math.sqrt(measurements.size())).toArray();
  }

  final String toStringRMS() {
    double[] rms = getRMS();
    String format = (rms.length > 1) ? "RMS = [%s] %%" : "RMS = %s %%";
    return format.formatted(
        Arrays.stream(rms)
            .map(ones -> Metrics.Dimensionless.ONE.to(ones, PERCENT))
            .mapToObj("%.1f"::formatted).collect(Collectors.joining("; "))
    );
  }

  final Collection<Measurement> measurements() {
    return measurements;
  }
}

