package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;

import java.util.Objects;

public sealed interface TetrapolarMeasurement {
  double ohms();

  double ohmsDiff();

  sealed interface Step1 {
    Step2 ohms(double rOhms);
  }

  sealed interface Step2 {
    Step3 thenOhms(double thenOhms);
  }

  sealed interface Step3 extends Builder<TetrapolarMeasurement> {
    Builder<TetrapolarDiffMeasurement> hDiff(double hDiff, Metrics.Length units);
  }

  static Step1 builder() {
    return new TetrapolarMeasurementBuilder();
  }

  final class TetrapolarMeasurementBuilder implements Step1, Step2, Step3 {
    private record TetrapolarMeasurementRecord(double ohms, double ohmsDiff) implements TetrapolarMeasurement {
    }

    private double ohms;
    private double ohmsDiff;

    private TetrapolarMeasurementBuilder() {
    }

    @Override
    public Step2 ohms(double ohms) {
      this.ohms = ohms;
      return this;
    }

    @Override
    public Step3 thenOhms(double thenOhms) {
      ohmsDiff = thenOhms - ohms;
      return this;
    }

    @Override
    public TetrapolarMeasurement build() {
      return new TetrapolarMeasurementRecord(ohms, ohmsDiff);
    }

    @Override
    public Builder<TetrapolarDiffMeasurement> hDiff(double hDiff, Metrics.Length units) {
      return new TetrapolarDiffMeasurement.TetrapolarDiffMeasurementBuilder(build(), units.toSI(hDiff));
    }
  }

  sealed interface TetrapolarDiffMeasurement extends TetrapolarMeasurement {
    double hDiff();

    final class TetrapolarDiffMeasurementBuilder implements Builder<TetrapolarDiffMeasurement> {
      private record TetrapolarDiffMeasurementRecord(TetrapolarMeasurement measurement, double hDiff)
          implements TetrapolarDiffMeasurement {
        private TetrapolarDiffMeasurementRecord {
          Objects.requireNonNull(measurement);
        }

        @Override
        public double ohms() {
          return measurement().ohms();
        }

        @Override
        public double ohmsDiff() {
          return measurement().ohmsDiff();
        }
      }

      private final TetrapolarMeasurement measurement;
      private final double hDiff;

      public TetrapolarDiffMeasurementBuilder(TetrapolarMeasurement measurement, double hDiff) {
        this.measurement = measurement;
        this.hDiff = hDiff;
      }

      @Override
      public TetrapolarDiffMeasurement build() {
        return new TetrapolarDiffMeasurementRecord(measurement, hDiff);
      }
    }
  }
}
