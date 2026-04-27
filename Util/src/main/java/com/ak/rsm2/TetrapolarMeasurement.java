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

    Builder<TetrapolarMaxDiffAbsoluteMeasurement> hDiffMaxAbsolute(double hDiffMax, Metrics.Length units);

    Builder<TetrapolarMaxDiffRelativeMeasurement> hDiffMaxRelative(double hDiffMax, Metrics.Length units);
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

    @Override
    public Builder<TetrapolarMaxDiffAbsoluteMeasurement> hDiffMaxAbsolute(double hDiffMax, Metrics.Length units) {
      return new TetrapolarMaxDiffAbsoluteMeasurement.TetrapolarMaxDiffAbsoluteMeasurementBuilder(build(), units.toSI(hDiffMax));
    }

    @Override
    public Builder<TetrapolarMaxDiffRelativeMeasurement> hDiffMaxRelative(double hDiffMax, Metrics.Length units) {
      return new TetrapolarMaxDiffRelativeMeasurement.TetrapolarMaxDiffRelativeMeasurementBuilder(build(), units.toSI(hDiffMax));
    }
  }

  abstract sealed class AbstractTetrapolarDiffMeasurement implements TetrapolarMeasurement {
    private final TetrapolarMeasurement measurement;
    private final double hDiff;

    protected AbstractTetrapolarDiffMeasurement(TetrapolarMeasurement measurement, double hDiff) {
      this.measurement = Objects.requireNonNull(measurement);
      this.hDiff = hDiff;
    }

    @Override
    public final double ohms() {
      return measurement.ohms();
    }

    @Override
    public final double ohmsDiff() {
      return measurement.ohmsDiff();
    }

    protected final double getHDiff() {
      return hDiff;
    }
  }

  sealed interface TetrapolarDiffMeasurement extends TetrapolarMeasurement {
    double hDiff();

    final class TetrapolarDiffMeasurementBuilder implements Builder<TetrapolarDiffMeasurement> {
      private static final class TetrapolarDiffMeasurementRecord extends AbstractTetrapolarDiffMeasurement
          implements TetrapolarDiffMeasurement {
        private TetrapolarDiffMeasurementRecord(TetrapolarMeasurement measurement, double hDiff) {
          super(measurement, hDiff);
        }

        @Override
        public double hDiff() {
          return getHDiff();
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

  sealed interface TetrapolarMaxDiffAbsoluteMeasurement extends TetrapolarMeasurement {
    double hDiffMax();

    final class TetrapolarMaxDiffAbsoluteMeasurementBuilder implements Builder<TetrapolarMaxDiffAbsoluteMeasurement> {
      private static final class TetrapolarMaxDiffAbsoluteMeasurementRecord extends AbstractTetrapolarDiffMeasurement
          implements TetrapolarMaxDiffAbsoluteMeasurement {
        private TetrapolarMaxDiffAbsoluteMeasurementRecord(TetrapolarMeasurement measurement, double hDiffMax) {
          super(measurement, hDiffMax);
        }

        @Override
        public double hDiffMax() {
          return getHDiff();
        }
      }

      private final TetrapolarMeasurement measurement;
      private final double hDiffMax;

      public TetrapolarMaxDiffAbsoluteMeasurementBuilder(TetrapolarMeasurement measurement, double hDiffMax) {
        this.measurement = measurement;
        this.hDiffMax = hDiffMax;
      }

      @Override
      public TetrapolarMaxDiffAbsoluteMeasurement build() {
        return new TetrapolarMaxDiffAbsoluteMeasurementRecord(measurement, hDiffMax);
      }
    }
  }

  sealed interface TetrapolarMaxDiffRelativeMeasurement extends TetrapolarMeasurement {
    double hDiffMax();

    final class TetrapolarMaxDiffRelativeMeasurementBuilder implements Builder<TetrapolarMaxDiffRelativeMeasurement> {
      private static final class TetrapolarMaxDiffRelativeMeasurementRecord extends AbstractTetrapolarDiffMeasurement
          implements TetrapolarMaxDiffRelativeMeasurement {
        private TetrapolarMaxDiffRelativeMeasurementRecord(TetrapolarMeasurement measurement, double hDiffMax) {
          super(measurement, hDiffMax);
        }

        @Override
        public double hDiffMax() {
          return getHDiff();
        }
      }

      private final TetrapolarMeasurement measurement;
      private final double hDiffMax;

      public TetrapolarMaxDiffRelativeMeasurementBuilder(TetrapolarMeasurement measurement, double hDiffMax) {
        this.measurement = measurement;
        this.hDiffMax = hDiffMax;
      }

      @Override
      public TetrapolarMaxDiffRelativeMeasurement build() {
        return new TetrapolarMaxDiffRelativeMeasurementRecord(measurement, hDiffMax);
      }
    }
  }
}
