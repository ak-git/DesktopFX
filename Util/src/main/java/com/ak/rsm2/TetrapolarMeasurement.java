package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;

public sealed interface TetrapolarMeasurement {
  double ohms();

  double ohmsDiff();

  double hDiff();

  sealed interface Step1 {
    Step2 ohms(double rOhms);
  }

  sealed interface Step2 {
    Step3 thenOhms(double thenOhms);
  }

  sealed interface Step3 {
    Builder<TetrapolarMeasurement> hDiff(double hDiff);
  }

  static Step1 builder(Metrics.Length units) {
    return new TetrapolarMeasurementBuilder(units);
  }

  final class TetrapolarMeasurementBuilder implements Step1, Step2, Step3, Builder<TetrapolarMeasurement> {
    private record TetrapolarMeasurementRecord(double ohms, double ohmsDiff,
                                               double hDiff) implements TetrapolarMeasurement {
    }

    private final Metrics.Length units;
    private double ohms;
    private double ohmsDiff;
    private double hDiff;

    private TetrapolarMeasurementBuilder(Metrics.Length units) {
      this.units = units;
    }

    @Override
    public Step2 ohms(double ohms) {
      this.ohms = ohms;
      return this;
    }

    @Override
    public Builder<TetrapolarMeasurement> hDiff(double hDiff) {
      this.hDiff = units.toSI(hDiff);
      return this;
    }

    @Override
    public Step3 thenOhms(double thenOhms) {
      ohmsDiff = thenOhms - ohms;
      return this;
    }

    @Override
    public TetrapolarMeasurement build() {
      return new TetrapolarMeasurementRecord(ohms, ohmsDiff, hDiff);
    }
  }
}
