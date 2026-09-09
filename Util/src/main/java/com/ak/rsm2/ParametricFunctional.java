package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.log1p;

public sealed interface ParametricFunctional {
  enum Regularization {
    ZERO_MAX_LOG
  }

  double dataErrorNorm();

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
        this.system = Objects.requireNonNull(system);
        this.measurement = Objects.requireNonNull(measurement);
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

      protected final double misfit(Model model, TetrapolarMeasurement m, double dh) {
        Resistivity.Step1 r = Resistivity.of(system);
        Resistivity.Apparent resistivity = r.apparent(model);
        double apparent = r.apparent(m.ohms());
        double derivativeApparentByPhi = r.apparent((m.ohmsDiff() / dh) / system.phiFactor());
        double v = log(resistivity.value() / apparent) - log(resistivity.derivative() / derivativeApparentByPhi);
        return Double.isNaN(v) ? Double.POSITIVE_INFINITY : Math.abs(v);
      }

      protected static double regularization(double x, double min, double max) {
        double s = log(max - x) - log(x - min);
        return Double.isFinite(s) ? s * s : Double.POSITIVE_INFINITY;
      }

      private static final class Diff extends AbstractParametricFunctional<TetrapolarMeasurement.Diff> {
        private Diff(ElectrodeSystem.Inexact system, TetrapolarMeasurement.Diff measurement) {
          super(system, measurement);
        }

        @Override
        public double dataErrorNorm() {
          return log1p(system().apparentRhoRelativeError());
        }

        @Override
        public ToDoubleFunction<Model> misfit() {
          return layer -> {
            if (Objects.requireNonNull(layer) instanceof Model.Layer2Relative layer2Relative) {
              return misfit(layer2Relative, measurement(), measurement().hDiff());
            }
            throw new IllegalStateException("Unexpected value: " + layer);
          };
        }

        @Override
        public ToDoubleFunction<Model> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer -> {
              if (Objects.requireNonNull(layer) instanceof Model.Layer2Relative(K k, double h)) {
                return regularization(k, h);
              }
              throw new IllegalArgumentException("Unexpected value: " + layer);
            };
          };
        }
      }

      private abstract static sealed class AbstractDiffRelative<M extends TetrapolarMeasurement> extends AbstractParametricFunctional<M> {
        private AbstractDiffRelative(ElectrodeSystem.Inexact system, M measurement) {
          super(system, measurement);
        }

        @Override
        public final double dataErrorNorm() {
          return log1p(system().apparentRhoRelativeError());
        }

        @Override
        public final ToDoubleFunction<Model> misfit() {
          return layer -> {
            if (Objects.requireNonNull(layer) instanceof Model.Layer2RelativeDh layer2RelativeDH) {
              return misfit(layer2RelativeDH, measurement(), layer2RelativeDH.dh());
            }
            throw new IllegalStateException("Unexpected value: " + layer);
          };
        }
      }

      private static final class MaxDiffRelative extends AbstractDiffRelative<TetrapolarMeasurement.MaxDiff> {
        private MaxDiffRelative(ElectrodeSystem.Inexact system, TetrapolarMeasurement.MaxDiff measurement) {
          super(system, measurement);
        }

        @Override
        public ToDoubleFunction<Model> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer -> {
              if (Objects.requireNonNull(layer) instanceof Model.Layer2RelativeDh(
                  Model.Layer2Relative layer2Relative, double dh
              )) {
                return regularization(layer2Relative.k(), layer2Relative.h()) +
                    regularization(Math.abs(dh), 0, 2.0 * Math.abs(measurement().hDiffMax()));
              }
              throw new IllegalArgumentException("Unexpected value: " + layer);
            };
          };
        }
      }

      private static final class ZeroDiffRelative extends AbstractDiffRelative<TetrapolarMeasurement.ZeroDiff> {
        private ZeroDiffRelative(ElectrodeSystem.Inexact system, TetrapolarMeasurement.ZeroDiff measurement) {
          super(system, measurement);
        }

        @Override
        public ToDoubleFunction<Model> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer -> {
              if (Objects.requireNonNull(layer) instanceof Model.Layer2RelativeDh(
                  Model.Layer2Relative layer2Relative, double dh
              )) {
                return regularization(layer2Relative.k(), layer2Relative.h()) +
                    regularization(dh, -Math.abs(measurement().hDiffZero()), Math.abs(measurement().hDiffZero()));
              }
              throw new IllegalArgumentException("Unexpected value: " + layer);
            };
          };
        }
      }

      private static final class TwoMaxDiffRelative extends AbstractParametricFunctional<TetrapolarMeasurement.TwoMaxDiff> {
        private TwoMaxDiffRelative(ElectrodeSystem.Inexact system, TetrapolarMeasurement.TwoMaxDiff measurement) {
          super(system, measurement);
        }

        @Override
        public double dataErrorNorm() {
          return log1p(system().apparentRhoRelativeError());
        }

        @Override
        public ToDoubleFunction<Model> misfit() {
          return layer -> {
            if (Objects.requireNonNull(layer) instanceof Model.Layer3AbsoluteDRho2(
                Model.Layer3Absolute layer3Absolute, Model.P dp, double dRho2
            )) {
              if (layer3Absolute.rho1() < layer3Absolute.rho2() && layer3Absolute.rho2() > layer3Absolute.rho3() &&
                  layer3Absolute.p().p1() < layer3Absolute.p().p2mp1() &&
                  dp.p1() < dp.p2mp1() &&
                  dp.pSum() <= measurement().hDiffMax() / layer3Absolute.hStep()) {
                TetrapolarMeasurement mRho2 = TetrapolarMeasurement.builder().ohms(measurement().ohms() + measurement().ohmsDiff())
                    .thenOhms(measurement().next().ohms()).build();
                return DoubleStream.of(
                        misfitLog(layer3Absolute, layer3Absolute.toModel(dp, 0.0), measurement()),
                        misfitLog(layer3Absolute.toModel(dp, 0.0), layer3Absolute.toModel(dp, dRho2), mRho2),
                        misfitLog(layer3Absolute.toModel(dp, dRho2), layer3Absolute.toModel(dp.multiply(2.0), dRho2), measurement().next())
                    )
                    .reduce(StrictMath::hypot).orElseThrow();
              }
              else {
                return Double.POSITIVE_INFINITY;
              }
            }
            throw new IllegalStateException("Unexpected value: " + layer);
          };
        }

        private double misfitLog(Model model, Model model2, TetrapolarMeasurement m) {
          Resistivity.Step1 r = Resistivity.of(system());
          Resistivity.Apparent resistivity = r.apparent(model);
          Resistivity.Apparent resistivity2 = r.apparent(model2);
          double v = StrictMath.hypot(
              log(resistivity.value() / r.apparent(m.ohms())),
              log((resistivity2.value() - resistivity.value()) / r.apparent(m.ohmsDiff()))
          );
          return Double.isNaN(v) ? Double.POSITIVE_INFINITY : v;
        }

        @Override
        public ToDoubleFunction<Model> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer -> {
              if (Objects.requireNonNull(layer) instanceof
                  Model.Layer3AbsoluteDRho2(Model.Layer3Absolute layer3Absolute, Model.P dp, _)) {
                double rho1 = layer3Absolute.rho1();
                double rho2 = layer3Absolute.rho2();
                double rho3 = layer3Absolute.rho3();
                double hStep = layer3Absolute.hStep();
                Model.P p = layer3Absolute.p();
                return regularization(K.of(rho1, rho2), layer3Absolute.p().p1() * hStep) +
                    regularization(K.of(rho2, rho3), p.pSum() * hStep) +
                    regularization(Math.abs(hStep * dp.pSum()), 0, 2.0 * Math.abs(measurement().hDiffMax()));
              }
              throw new IllegalStateException("Unexpected value: " + layer);
            };
          };
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
        case TetrapolarMeasurement.TwoMaxDiff twoMaxDiff ->
            new AbstractParametricFunctional.TwoMaxDiffRelative(s, twoMaxDiff);
        case TetrapolarMeasurement.MaxDiff maxDiffRelative ->
            new AbstractParametricFunctional.MaxDiffRelative(s, maxDiffRelative);
        case TetrapolarMeasurement.ZeroDiff zeroDiffRelative ->
            new AbstractParametricFunctional.ZeroDiffRelative(s, zeroDiffRelative);
        case TetrapolarMeasurement.Diff diff -> new AbstractParametricFunctional.Diff(s, diff);
        case TetrapolarMeasurement tetrapolarMeasurement ->
            throw new IllegalArgumentException("Unexpected value: " + tetrapolarMeasurement);
      };
    }
  }
}
