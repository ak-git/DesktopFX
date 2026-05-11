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
    Builder<Diff> hDiff(double hDiff, Metrics.Length units);

    Builder<MaxDiff> hDiffMax(double hDiffMax, Metrics.Length units);
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
    public Builder<Diff> hDiff(double hDiff, Metrics.Length units) {
      return () -> new Diff.DiffRecord(build(), units.toSI(hDiff));
    }

    @Override
    public Builder<MaxDiff> hDiffMax(double hDiffMax, Metrics.Length units) {
      return () -> new MaxDiff.MaxDiffRecord(build(), units.toSI(hDiffMax));
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

    public final double hDiff() {
      return hDiff;
    }
  }

  sealed interface Diff extends TetrapolarMeasurement {
    double hDiff();

    final class DiffRecord extends AbstractTetrapolarDiffMeasurement
        implements Diff {
      private DiffRecord(TetrapolarMeasurement measurement, double hDiff) {
        super(measurement, hDiff);
      }
    }
  }

  sealed interface MaxDiff extends TetrapolarMeasurement {
    double hDiffMax();

    final class MaxDiffRecord extends AbstractTetrapolarDiffMeasurement
        implements MaxDiff {
      private MaxDiffRecord(TetrapolarMeasurement measurement, double hDiffMax) {
        super(measurement, hDiffMax);
      }

      @Override
      public double hDiffMax() {
        return hDiff();
      }
    }
  }
}
