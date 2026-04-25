package com.ak.rsm2;

import com.ak.math.Simplex;
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

  Simplex.Bounds[] bounds();

  ToDoubleFunction<Model> misfit();

  ToDoubleFunction<Model> regularization(Regularization regularization);

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
    private abstract static sealed class AbstractParametricOperator<M extends TetrapolarMeasurement> implements ParametricOperator {
      private final ElectrodeSystem.Inexact system;
      private final M measurement;

      private AbstractParametricOperator(ElectrodeSystem.Inexact system, M measurement) {
        this.system = system;
        this.measurement = measurement;
      }

      protected final ElectrodeSystem.Inexact system() {
        return system;
      }

      protected final M measurement() {
        return measurement;
      }

      @Override
      public final double dataErrorNorm() {
        return system.dataErrorNorm();
      }

      @Override
      public final ToDoubleFunction<Model> regularization(Regularization regularization) {
        return switch (regularization) {
          case ZERO_MAX_LOG -> layer -> {
            if (layer instanceof Model.Layer2Relative(K k, double h)) {
              double hMin = system.hMin(k);
              double hMax = system.hMax(k);
              if (hMin < h && h < hMax) {
                double x = log(h);
                double s = log(log(hMax) - x) - log(x - log(hMin));
                return s * s;
              }
              else {
                return Double.POSITIVE_INFINITY;
              }
            }
            else {
              return 0.0;
            }
          };
        };
      }

      private static final class ParametricMaxDiffOperator extends AbstractParametricOperator<TetrapolarMeasurement.TetrapolarMaxDiffMeasurement> {
        private ParametricMaxDiffOperator(ElectrodeSystem.Inexact system,
                                          TetrapolarMeasurement.TetrapolarMaxDiffMeasurement measurement) {
          super(system, measurement);
        }

        @Override
        public Simplex.Bounds[] bounds() {
          return new Simplex.Bounds[] {
              new Simplex.Bounds(0.0, 10.0), new Simplex.Bounds(0.0, 10.0),
              new Simplex.Bounds(0.0, system().hMax(K.PLUS_ONE)),
              new Simplex.Bounds(0.0, measurement().hDiffMax())
          };
        }

        @Override
        public ToDoubleFunction<Model> misfit() {
          return layer -> {
            switch (layer) {
              case Model.Layer2Relative layer2Relative ->
                  throw new IllegalStateException("Unexpected value: " + layer2Relative);
              case Model.Layer2Absolute layer2Absolute -> {
                Resistivity.Apparent resistivity = Resistivity.of(system()).apparent(layer2Absolute);
                double apparent = resistivity.apparent(measurement().ohms());
                double derivativeApparentByPhi = resistivity.apparent((measurement().ohmsDiff() / layer2Absolute.dh()) / system().phiFactor());
                double v = log(resistivity.value() / apparent) + log(resistivity.derivativeByPhi() / derivativeApparentByPhi);
                return Double.isNaN(v) ? Double.POSITIVE_INFINITY : Math.abs(v);
              }
            }
          };
        }
      }


      private static final class ParametricDiffOperator extends AbstractParametricOperator<TetrapolarMeasurement.TetrapolarDiffMeasurement> {
        private ParametricDiffOperator(ElectrodeSystem.Inexact system,
                                       TetrapolarMeasurement.TetrapolarDiffMeasurement measurement) {
          super(system, measurement);
        }

        @Override
        public Simplex.Bounds[] bounds() {
          return new Simplex.Bounds[] {
              new Simplex.Bounds(-1.0, 1.0), new Simplex.Bounds(0.0, system().hMax(K.PLUS_ONE))
          };
        }

        @Override
        public ToDoubleFunction<Model> misfit() {
          return layer -> {
            switch (layer) {
              case Model.Layer2Relative layer2Relative -> {
                Resistivity.Apparent resistivity = Resistivity.of(system()).apparentDivRho1(layer2Relative);
                double apparent = resistivity.apparent(measurement().ohms());
                double derivativeApparentByPhi = resistivity.apparent((measurement().ohmsDiff() / measurement().hDiff()) / system().phiFactor());
                double v = log(resistivity.value() / apparent) -
                    log(resistivity.derivativeByPhi() / derivativeApparentByPhi);
                return Double.isNaN(v) ? Double.POSITIVE_INFINITY : Math.abs(v);
              }
              case Model.Layer2Absolute layer2Absolute ->
                  throw new IllegalStateException("Unexpected value: " + layer2Absolute);
            }
          };
        }
      }

      private static final class ParametricStaticOperator extends AbstractParametricOperator<TetrapolarMeasurement> {
        private ParametricStaticOperator(ElectrodeSystem.Inexact system, TetrapolarMeasurement measurement) {
          super(system, measurement);
        }

        @Override
        public Simplex.Bounds[] bounds() {
          return new Simplex.Bounds[] {
              new Simplex.Bounds(0.0, 10.0),
              new Simplex.Bounds(0.0, 20.0),
              new Simplex.Bounds(0.0, 50.0),
              new Simplex.Bounds(0.0, system().hMax(K.PLUS_ONE))
          };
        }

        @Override
        public ToDoubleFunction<Model> misfit() {
          return layer -> {
            switch (layer) {
              case Model.Layer2Relative layer2Relative ->
                  throw new IllegalStateException("Unexpected value: " + layer2Relative);
              case Model.Layer2Absolute layer2Absolute ->
                  throw new IllegalStateException("Unexpected value: " + layer2Absolute);
            }
          };
        }
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
      ElectrodeSystem.Inexact s = Objects.requireNonNull(system);
      return switch (Objects.requireNonNull(measurement)) {
        case TetrapolarMeasurement.TetrapolarMaxDiffMeasurement tetrapolarMaxDiffMeasurement ->
            new AbstractParametricOperator.ParametricMaxDiffOperator(s, tetrapolarMaxDiffMeasurement);
        case TetrapolarMeasurement.TetrapolarDiffMeasurement tetrapolarDiffMeasurement ->
            new AbstractParametricOperator.ParametricDiffOperator(s, tetrapolarDiffMeasurement);
        case TetrapolarMeasurement tetrapolarMeasurement ->
            new AbstractParametricOperator.ParametricStaticOperator(s, tetrapolarMeasurement);
      };
    }
  }
}
