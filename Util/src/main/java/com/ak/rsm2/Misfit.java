package com.ak.rsm2;

import com.ak.util.Builder;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.log1p;

public sealed interface Misfit {
  ToDoubleFunction<Model.Layer2Relative> errorLog();

  double dataNorm();

  sealed interface Step1 {
    Step2 ofMilli(Function<ElectrodeSystem.Step1, Builder<ElectrodeSystem.Inexact>> builderFunction);
  }

  sealed interface Step2 {
    Builder<Misfit> measurements(Function<TetrapolarMeasurement.Step1, Builder<TetrapolarMeasurement>> builderFunction);
  }

  static Step1 builder() {
    return new MisfitBuilder();
  }

  final class MisfitBuilder implements Step1, Step2, Builder<Misfit> {
    private record MisfitRecord(ElectrodeSystem.Inexact system, TetrapolarMeasurement measurement) implements Misfit {
      @Override
      public ToDoubleFunction<Model.Layer2Relative> errorLog() {
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
      public double dataNorm() {
        return log1p(system.apparentRhoRelativeError());
      }
    }

    private ElectrodeSystem.@Nullable Inexact system;
    private @Nullable TetrapolarMeasurement measurement;

    private MisfitBuilder() {
    }

    @Override
    public Step2 ofMilli(Function<ElectrodeSystem.Step1, Builder<ElectrodeSystem.Inexact>> builderFunction) {
      system = builderFunction.apply(ElectrodeSystem.ofMilli()).build();
      return this;
    }

    @Override
    public Builder<Misfit> measurements(Function<TetrapolarMeasurement.Step1, Builder<TetrapolarMeasurement>> builderFunction) {
      measurement = builderFunction.apply(TetrapolarMeasurement.builder()).build();
      return this;
    }

    @Override
    public Misfit build() {
      return new MisfitRecord(Objects.requireNonNull(system), Objects.requireNonNull(measurement));
    }
  }
}
