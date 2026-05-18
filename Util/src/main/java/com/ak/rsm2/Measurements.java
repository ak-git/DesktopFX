package com.ak.rsm2;

import com.ak.util.Builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public sealed interface Measurements<M extends TetrapolarMeasurement> {
  Collection<M> measurements();

  static <M extends TetrapolarMeasurement> Step1<M> builder() {
    return new MeasurementsBuilder<>();
  }

  sealed interface Step1<M extends TetrapolarMeasurement> extends Builder<Measurements<M>> {
    Step1<M> add(Function<TetrapolarMeasurement.Step1, Builder<? extends M>> builderFunction);
  }

  final class MeasurementsBuilder<M extends TetrapolarMeasurement> implements Step1<M> {
    private record MeasurementsRecord<M extends TetrapolarMeasurement>(Collection<M> measurements)
        implements Measurements<M> {
      private MeasurementsRecord {
        if (measurements.isEmpty()) {
          throw new IllegalArgumentException("measurements must not be empty");
        }
      }
    }

    private final List<M> measurements = new ArrayList<>();

    @Override
    public Step1<M> add(Function<TetrapolarMeasurement.Step1, Builder<? extends M>> builderFunction) {
      measurements.add(builderFunction.apply(TetrapolarMeasurement.builder()).build());
      return this;
    }

    @Override
    public Measurements<M> build() {
      return new MeasurementsRecord<>(Collections.unmodifiableList(measurements));
    }
  }
}
