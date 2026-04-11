package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;

public sealed interface TetrapolarMeasurement {
  double ohms();

  double dOhms();

  double dh();

  sealed interface Step1 {
    Step2 ohms(double rOhms);
  }

  sealed interface Step2 {
    Step3 dh(double dhMilli);
  }

  sealed interface Step3 {
    Builder<TetrapolarMeasurement> thenOhms(double thenOhms);
  }

  static Step1 builder(Metrics.Length units) {
    return new TetrapolarMeasurementBuilder(units);
  }

  final class TetrapolarMeasurementBuilder implements Step1, Step2, Step3, Builder<TetrapolarMeasurement> {
    private record TetrapolarMeasurementRecord(double ohms, double dOhms, double dh) implements TetrapolarMeasurement {
    }

    private final Metrics.Length units;
    private double rOhms;
    private double dh;
    private double dOhms;

    private TetrapolarMeasurementBuilder(Metrics.Length units) {
      this.units = units;
    }

    @Override
    public Step2 ohms(double rOhms) {
      this.rOhms = rOhms;
      return this;
    }

    @Override
    public Step3 dh(double dh) {
      this.dh = units.toSI(dh);
      return this;
    }

    @Override
    public Builder<TetrapolarMeasurement> thenOhms(double thenOhms) {
      dOhms = thenOhms - rOhms;
      return this;
    }

    @Override
    public TetrapolarMeasurement build() {
      return new TetrapolarMeasurementRecord(rOhms, dOhms, dh);
    }
  }
}
