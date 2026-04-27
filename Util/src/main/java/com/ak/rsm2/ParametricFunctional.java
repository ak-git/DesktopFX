package com.ak.rsm2;

import com.ak.math.Simplex;
import com.ak.util.Builder;
import com.ak.util.Metrics;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import static java.lang.StrictMath.*;

public sealed interface ParametricFunctional {
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
    Builder<ParametricFunctional> measurements(Function<TetrapolarMeasurement.Step1, Builder<? extends M>> builderFunction);
  }

  static <M extends TetrapolarMeasurement> Step1<M> builder(Metrics.Length units) {
    return new ParametricFunctionalBuilder<>(units);
  }

  final class ParametricFunctionalBuilder<M extends TetrapolarMeasurement> implements Step1<M>, Step2<M>, Builder<ParametricFunctional> {
    private abstract static sealed class AbstractParametricFunctional<M extends TetrapolarMeasurement> implements ParametricFunctional {
      private final ElectrodeSystem.Inexact system;
      private final M measurement;

      private AbstractParametricFunctional(ElectrodeSystem.Inexact system, M measurement) {
        this.system = system;
        this.measurement = measurement;
      }

      protected final ElectrodeSystem.Inexact system() {
        return system;
      }

      protected final M measurement() {
        return measurement;
      }

      protected final double regularization(K k, double h) {
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

      protected static double regularization(double dh, double dhMax) {
        double x = Math.abs(dh);
        double max = Math.abs(dhMax);
        double s = log(2.0 * max - x) - log(x);
        return Double.isFinite(s) ? s * s : Double.POSITIVE_INFINITY;
      }

      private static final class ParametricDiffFunctional
          extends AbstractParametricFunctional<TetrapolarMeasurement.DiffMeasurement> {
        private ParametricDiffFunctional(ElectrodeSystem.Inexact system,
                                         TetrapolarMeasurement.DiffMeasurement measurement) {
          super(system, measurement);
        }

        @Override
        public double dataErrorNorm() {
          return log1p(system().apparentRhoRelativeError());
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
                double v = log(resistivity.value() / apparent) - log(resistivity.derivativeByPhi() / derivativeApparentByPhi);
                return Double.isNaN(v) ? Double.POSITIVE_INFINITY : Math.abs(v);
              }
              case Model.Layer2RelativeDH layer2RelativeDH ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2RelativeDH);
              case Model.Layer2Absolute layer2Absolute ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2Absolute);
            }
          };
        }

        @Override
        public ToDoubleFunction<Model> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer ->
                switch (layer) {
                  case Model.Layer2Relative(K k, double h) -> regularization(k, h);
                  case Model.Layer2RelativeDH layer2RelativeDH ->
                      throw new IllegalArgumentException("Unexpected value: " + layer2RelativeDH);
                  case Model.Layer2Absolute layer2Absolute ->
                      throw new IllegalArgumentException("Unexpected value: " + layer2Absolute);
                };
          };
        }
      }

      private static final class ParametricMaxDiffAbsoluteFunctional
          extends AbstractParametricFunctional<TetrapolarMeasurement.MaxDiffAbsoluteMeasurement> {
        private ParametricMaxDiffAbsoluteFunctional(ElectrodeSystem.Inexact system,
                                                    TetrapolarMeasurement.MaxDiffAbsoluteMeasurement measurement) {
          super(system, measurement);
        }

        @Override
        public double dataErrorNorm() {
          return log1p(system().apparentRhoRelativeError());
        }

        @Override
        public Simplex.Bounds[] bounds() {
          return new Simplex.Bounds[] {
              new Simplex.Bounds(0.0, 10.0), new Simplex.Bounds(0.0, 10.0),
              new Simplex.Bounds(0.0, system().hMax(K.PLUS_ONE)),
              new Simplex.Bounds(Math.min(measurement().hDiffMax(), 0.0), Math.max(0.0, measurement().hDiffMax())),
          };
        }

        @Override
        public ToDoubleFunction<Model> misfit() {
          return layer -> {
            switch (layer) {
              case Model.Layer2Relative layer2Relative ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2Relative);
              case Model.Layer2RelativeDH layer2RelativeDH ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2RelativeDH);
              case Model.Layer2Absolute layer2Absolute -> {
                Resistivity.Apparent resistivity = Resistivity.of(system()).apparent(layer2Absolute);
                double apparent = resistivity.apparent(measurement().ohms());
                double derivativeApparentByPhi = resistivity.apparent((measurement().ohmsDiff() / layer2Absolute.dh()) / system().phiFactor());
                double v = hypot(log(resistivity.value() / apparent), log(resistivity.derivativeByPhi() / derivativeApparentByPhi));
                return Double.isNaN(v) ? Double.POSITIVE_INFINITY : v;
              }
            }
          };
        }

        @Override
        public ToDoubleFunction<Model> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer -> switch (layer) {
              case Model.Layer2Relative layer2Relative ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2Relative);
              case Model.Layer2RelativeDH layer2RelativeDH ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2RelativeDH);
              case Model.Layer2Absolute(double rho1, double rho2, double h, double dh) ->
                  regularization(K.of(rho1, rho2), h) + regularization(dh, measurement().hDiffMax());
            };
          };
        }
      }

      private static final class ParametricDiffRelativeFunctional
          extends AbstractParametricFunctional<TetrapolarMeasurement.MaxDiffRelativeMeasurement> {
        private ParametricDiffRelativeFunctional(ElectrodeSystem.Inexact system,
                                                 TetrapolarMeasurement.MaxDiffRelativeMeasurement measurement) {
          super(system, measurement);
        }

        @Override
        public double dataErrorNorm() {
          return log1p(system().apparentRhoRelativeError());
        }

        @Override
        public Simplex.Bounds[] bounds() {
          return new Simplex.Bounds[] {
              new Simplex.Bounds(-1.0, 1.0),
              new Simplex.Bounds(0.0, system().hMax(K.PLUS_ONE)),
              new Simplex.Bounds(Math.min(measurement().hDiffMax(), 0.0), Math.max(0.0, measurement().hDiffMax()))
          };
        }

        @Override
        public ToDoubleFunction<Model> misfit() {
          return layer -> {
            switch (layer) {
              case Model.Layer2Relative layer2Relative ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2Relative);
              case Model.Layer2RelativeDH layer2RelativeDH -> {
                Resistivity.Apparent resistivity = Resistivity.of(system()).apparentDivRho1(layer2RelativeDH);
                double apparent = resistivity.apparent(measurement().ohms());
                double derivativeApparentByPhi = resistivity.apparent((measurement().ohmsDiff() / layer2RelativeDH.dh()) / system().phiFactor());
                double v = log(resistivity.value() / apparent) - log(resistivity.derivativeByPhi() / derivativeApparentByPhi);
                return Double.isNaN(v) ? Double.POSITIVE_INFINITY : Math.abs(v);
              }
              case Model.Layer2Absolute layer2Absolute ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2Absolute);
            }
          };
        }

        @Override
        public ToDoubleFunction<Model> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer ->
                switch (layer) {
                  case Model.Layer2Relative layer2Relative ->
                      throw new IllegalArgumentException("Unexpected value: " + layer2Relative);
                  case Model.Layer2RelativeDH(K k, double h, double dh) ->
                      regularization(k, h) + regularization(dh, measurement().hDiffMax());
                  case Model.Layer2Absolute layer2Absolute ->
                      throw new IllegalArgumentException("Unexpected value: " + layer2Absolute);
                };
          };
        }
      }

      private static final class ParametricStaticFunctional extends AbstractParametricFunctional<TetrapolarMeasurement> {
        private ParametricStaticFunctional(ElectrodeSystem.Inexact system, TetrapolarMeasurement measurement) {
          super(system, measurement);
        }

        @Override
        public double dataErrorNorm() {
          throw new UnsupportedOperationException("Not supported yet.");
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
                  throw new IllegalArgumentException("Unexpected value: " + layer2Relative);
              case Model.Layer2RelativeDH layer2RelativeDH ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2RelativeDH);
              case Model.Layer2Absolute layer2Absolute ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2Absolute);
            }
          };
        }

        @Override
        public ToDoubleFunction<Model> regularization(Regularization regularization) {
          throw new UnsupportedOperationException("Not supported yet.");
        }
      }
    }

    private final Metrics.Length units;
    private ElectrodeSystem.@Nullable Inexact system;
    private @Nullable M measurement;

    private ParametricFunctionalBuilder(Metrics.Length units) {
      this.units = units;
    }

    @Override
    public Step2<M> system(Function<ElectrodeSystem.Step1, Builder<ElectrodeSystem.Inexact>> builderFunction) {
      system = builderFunction.apply(ElectrodeSystem.builder(units)).build();
      return this;
    }

    @Override
    public Builder<ParametricFunctional> measurements(Function<TetrapolarMeasurement.Step1, Builder<? extends M>> builderFunction) {
      measurement = builderFunction.apply(TetrapolarMeasurement.builder()).build();
      return this;
    }

    @Override
    public ParametricFunctional build() {
      ElectrodeSystem.Inexact s = Objects.requireNonNull(system);
      return switch (Objects.requireNonNull(measurement)) {
        case TetrapolarMeasurement.MaxDiffAbsoluteMeasurement maxDiffAbsoluteMeasurement ->
            new AbstractParametricFunctional.ParametricMaxDiffAbsoluteFunctional(s, maxDiffAbsoluteMeasurement);
        case TetrapolarMeasurement.MaxDiffRelativeMeasurement maxDiffRelativeMeasurement ->
            new AbstractParametricFunctional.ParametricDiffRelativeFunctional(s, maxDiffRelativeMeasurement);
        case TetrapolarMeasurement.DiffMeasurement diffMeasurement ->
            new AbstractParametricFunctional.ParametricDiffFunctional(s, diffMeasurement);
        case TetrapolarMeasurement tetrapolarMeasurement ->
            new AbstractParametricFunctional.ParametricStaticFunctional(s, tetrapolarMeasurement);
      };
    }
  }
}
