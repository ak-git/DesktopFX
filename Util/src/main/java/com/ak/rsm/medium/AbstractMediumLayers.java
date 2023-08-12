package com.ak.rsm.medium;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.prediction.Prediction;
import com.ak.util.Metrics;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract sealed class AbstractMediumLayers implements MediumLayers, Function<Measurement, Prediction>
    permits Layer1Medium, Layer2Medium {
  @Nonnull
  private final Collection<Measurement> measurements;

  AbstractMediumLayers(@Nonnull Collection<? extends Measurement> measurements) {
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
        Arrays.stream(rms).map(Metrics::toPercents).mapToObj("%.1f"::formatted).collect(Collectors.joining("; "))
    );
  }

  @Nonnull
  final Collection<Measurement> measurements() {
    return measurements;
  }
}

