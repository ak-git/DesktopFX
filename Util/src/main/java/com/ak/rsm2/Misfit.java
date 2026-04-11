package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import static java.lang.StrictMath.log;

public sealed interface Misfit {
  enum Regularization {
    ZERO_MAX_LOG
  }

  double dataErrorNorm();

  double hMax();

  ToDoubleFunction<Model.Layer2Relative> misfit();

  ToDoubleFunction<Model.Layer2Relative> regularization(Regularization regularization);

  sealed interface Step1 {
    Step2 system(Function<ElectrodeSystem.Step1, Builder<ElectrodeSystem.Inexact>> builderFunction);
  }

  sealed interface Step2 {
    Builder<Misfit> measurements(Function<TetrapolarMeasurement.Step1, Builder<TetrapolarMeasurement>> builderFunction);
  }

  static Step1 builder(Metrics.Length units) {
    return new MisfitBuilder(units);
  }

  final class MisfitBuilder implements Step1, Step2, Builder<Misfit> {
    private record MisfitRecord(ElectrodeSystem.Inexact system, TetrapolarMeasurement measurement) implements Misfit {
      @Override
      public double dataErrorNorm() {
        return system.dataErrorNorm();
      }

      @Override
      public double hMax() {
        return system.hMax(K.PLUS_ONE);
      }

      @Override
      public ToDoubleFunction<Model.Layer2Relative> misfit() {
        Resistivity resistivity = Resistivity.of(system);
        double apparent = resistivity.apparent(measurement.ohms());
        double derivativeApparentByPhi = resistivity.apparent((measurement.dOhms() / measurement.dh()) / system.phiFactor());
        return layer2 -> {
          double v = log(resistivity.apparentDivRho1(layer2) / apparent) -
              log(resistivity.derivativeApparentByPhiDivRho1(layer2) / derivativeApparentByPhi);
          return Double.isNaN(v) ? Double.POSITIVE_INFINITY : Math.abs(v);
        };
      }

      @Override
      public ToDoubleFunction<Model.Layer2Relative> regularization(Regularization regularization) {
        return switch (regularization) {
          case ZERO_MAX_LOG -> layer2 -> {
            double hMin = system.hMin(layer2.k());
            double hMax = system.hMax(layer2.k());
            if (0 < hMin && hMin < layer2.h() && layer2.h() < hMax) {
              double x = log(layer2.h());
              double s = log(log(hMax) - x) - log(x - log(hMin));
              return s * s;
            }
            else {
              return Double.POSITIVE_INFINITY;
            }
          };
        };
      }
    }

    private final Metrics.Length units;
    private ElectrodeSystem.@Nullable Inexact system;
    private @Nullable TetrapolarMeasurement measurement;

    private MisfitBuilder(Metrics.Length units) {
      this.units = units;
    }

    @Override
    public Step2 system(Function<ElectrodeSystem.Step1, Builder<ElectrodeSystem.Inexact>> builderFunction) {
      system = builderFunction.apply(ElectrodeSystem.builder(units)).build();
      return this;
    }

    @Override
    public Builder<Misfit> measurements(Function<TetrapolarMeasurement.Step1, Builder<TetrapolarMeasurement>> builderFunction) {
      measurement = builderFunction.apply(TetrapolarMeasurement.builder(units)).build();
      return this;
    }

    @Override
    public Misfit build() {
      return new MisfitRecord(Objects.requireNonNull(system), Objects.requireNonNull(measurement));
    }
  }
}
