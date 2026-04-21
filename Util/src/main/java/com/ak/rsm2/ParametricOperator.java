package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import static java.lang.StrictMath.log;

public sealed interface ParametricOperator {
  enum Regularization {
    ZERO_MAX_LOG
  }

  double dataErrorNorm();

  double hMax();

  ToDoubleFunction<Model.Layer2Relative> misfit();

  ToDoubleFunction<Model.Layer2Relative> regularization(Regularization regularization);

  sealed interface Step1<M extends TetrapolarMeasurement> {
    Step2<M> system(Function<ElectrodeSystem.Step1, Builder<ElectrodeSystem.Inexact>> builderFunction);
  }

  sealed interface Step2<M extends TetrapolarMeasurement> {
    Builder<ParametricOperator> measurements(Function<TetrapolarMeasurement.Step1, Builder<? extends M>> builderFunction);
  }

  static <M extends TetrapolarMeasurement> Step1<M> builder(Metrics.Length units) {
    return new ParametricOperatorBuilder<>(units);
  }

  final class ParametricOperatorBuilder<M extends TetrapolarMeasurement> implements Step1<M>, Step2<M>, Builder<ParametricOperator> {
    private record ParametricOperatorDiffRecord(ElectrodeSystem.Inexact system,
                                                TetrapolarMeasurement.TetrapolarDiffMeasurement measurement)
        implements ParametricOperator {
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
        double derivativeApparentByPhi = resistivity.apparent((measurement.ohmsDiff() / measurement.hDiff()) / system.phiFactor());
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
            if (hMin < layer2.h() && layer2.h() < hMax) {
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
    private @Nullable M measurement;

    private ParametricOperatorBuilder(Metrics.Length units) {
      this.units = units;
    }

    @Override
    public Step2<M> system(Function<ElectrodeSystem.Step1, Builder<ElectrodeSystem.Inexact>> builderFunction) {
      system = builderFunction.apply(ElectrodeSystem.builder(units)).build();
      return this;
    }

    @Override
    public Builder<ParametricOperator> measurements(Function<TetrapolarMeasurement.Step1, Builder<? extends M>> builderFunction) {
      measurement = builderFunction.apply(TetrapolarMeasurement.builder()).build();
      return this;
    }

    @Override
    public ParametricOperator build() {
      return switch (Objects.requireNonNull(measurement)) {
        case TetrapolarMeasurement.TetrapolarDiffMeasurement tetrapolarDiffMeasurement ->
            new ParametricOperatorDiffRecord(Objects.requireNonNull(system), tetrapolarDiffMeasurement);
        default -> throw new IllegalStateException("Unexpected value: " + measurement);
      };
    }
  }
}
