package com.ak.rsm.medium;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.prediction.Prediction;
import com.ak.rsm.prediction.Predictions;
import com.ak.util.Strings;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Layer1Medium extends AbstractMediumLayers {
  private final ValuePair rho;

  public Layer1Medium(Collection<? extends Measurement> measurements) {
    super(measurements);
    Measurement average = Measurement.average(measurements);
    rho = ValuePair.Name.RHO.of(average.resistivity(), average.resistivity() * average.inexact().getApparentRelativeError());
  }

  @Override
  public ValuePair rho() {
    return rho;
  }

  @Override
  public String toString() {
    return Stream.of(
            rho, toStringRMS(),
            measurements().stream().map(Object::toString)
                .collect(Collectors.joining(Strings.NEW_LINE, Strings.NEW_LINE, Strings.EMPTY)))
        .map(Object::toString).collect(Collectors.joining(Strings.SEMICOLON));
  }

  @Override
  public Prediction apply(Measurement measurement) {
    return Predictions.of(measurement, rho.value());
  }
}

