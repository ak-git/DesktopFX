package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public sealed interface TetrapolarMeasurement {
  double ohms();

  double ohmsDiff();

  @Nullable
  default TetrapolarMeasurement next() {
    return null;
  }

  sealed interface Step1 {
    Step2 ohms(double rOhms);
  }

  sealed interface Step2 {
    Step3 thenOhms(double thenOhms);
  }

  sealed interface Step3 extends Builder<TetrapolarMeasurement> {
    Builder<Diff> hDiff(double hDiff, Metrics.Length units);

    Step4 hDiffMax(double hDiffMax, Metrics.Length units);
  }

  sealed interface Step4 extends Builder<MaxDiff> {
    Builder<MaxDiff> add(Function<Step1, Builder<MaxDiff>> builderFunction);
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
    public Step4 hDiffMax(double hDiffMax, Metrics.Length units) {
      return new MaxDiff.TwoMaxDiffBuilder(new MaxDiff.MaxDiffRecord(build(), units.toSI(hDiffMax)));
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

    final double getHDiff() {
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

      @Override
      public double hDiff() {
        return getHDiff();
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
        return getHDiff();
      }
    }

    final class TwoMaxDiffBuilder implements Step4 {
      private record TwoMaxDiffRecord(MaxDiff maxDiff1, MaxDiff maxDiff2) implements MaxDiff {
        @Override
        public double ohms() {
          return maxDiff1.ohms();
        }

        @Override
        public double ohmsDiff() {
          return maxDiff1.ohmsDiff();
        }

        @Override
        public double hDiffMax() {
          return maxDiff1.hDiffMax();
        }

        @Override
        public TetrapolarMeasurement next() {
          return maxDiff2;
        }
      }

      private final MaxDiff maxDiff;

      public TwoMaxDiffBuilder(MaxDiff maxDiff) {
        this.maxDiff = maxDiff;
      }

      @Override
      public Builder<MaxDiff> add(Function<Step1, Builder<MaxDiff>> builderFunction) {
        return () -> new TwoMaxDiffRecord(maxDiff, builderFunction.apply(builder()).build());
      }

      @Override
      public MaxDiff build() {
        return maxDiff;
      }
    }
  }
}
