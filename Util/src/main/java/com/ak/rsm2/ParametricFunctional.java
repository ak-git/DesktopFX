package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;
import com.ak.util.Numbers;
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

  ElectrodeSystem.Inexact system();

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

      @Override
      public final ElectrodeSystem.Inexact system() {
        return system;
      }

      protected final M measurement() {
        return measurement;
      }

      protected final double regularization2(K k, double h) {
        double r = regularization(k, h);
        if (Double.isFinite(r)) {
          return r + Numbers.regularization(Math.abs(k.value()), 0.0, 2.0);
        }
        else {
          return Double.POSITIVE_INFINITY;
        }
      }

      protected final double regularization(K k, double h) {
        double hMin = system.hMin(k);
        double hMax = system.hMax(k);
        if (hMin < h && h < hMax) {
          return Numbers.regularization(log(h), log(Math.max(hMin, Metrics.Length.MICRO.toSI(10.0))), log(hMax));
        }
        else {
          return Double.POSITIVE_INFINITY;
        }
      }

      protected final double regularization3(Model.Layer3Model model) {
        return regularization(model.k1(), model.p().p1() * model.hStep()) +
            regularization(model.k2(), model.p().pSum() * model.hStep());
      }

      protected final double misfit(Model model, TetrapolarMeasurement m, double dh) {
        Resistivity.Step1 r = Resistivity.of(system);
        Resistivity.Apparent resistivity = r.apparent(model);
        double apparent = r.apparent(m.ohms());
        double derivativeApparentByPhi = r.apparent((m.ohmsDiff() / dh) / system.phiFactor());

        double v = switch (model) {
          case Model.Layer2Relative _, Model.Layer2RelativeDh _, Model.Layer3RelativeDh _ ->
              log(resistivity.value() / apparent) - log(resistivity.derivative() / derivativeApparentByPhi);
          default -> throw new IllegalArgumentException("Unexpected value: " + model);
        };
        return Double.isNaN(v) ? Double.POSITIVE_INFINITY : Math.abs(v);
      }

      protected final double misfitLog(Model model, Model model2, TetrapolarMeasurement m) {
        Resistivity.Step1 r = Resistivity.of(system());
        Resistivity.Apparent resistivity = r.apparent(model);
        Resistivity.Apparent resistivity2 = r.apparent(model2);
        double v = StrictMath.hypot(
            log(resistivity.value() / r.apparent(m.ohms())),
            log((resistivity2.value() - resistivity.value()) / r.apparent(m.ohmsDiff()))
        );
        return Double.isNaN(v) ? Double.POSITIVE_INFINITY : v;
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
          return layer ->
              switch (Objects.requireNonNull(layer)) {
                case Model.Layer2Relative layer2Relative ->
                    misfit(layer2Relative, measurement(), measurement().hDiff());
                case Model.Layer3RelativeDh layer3RelativeDh -> {
                  Model.Layer3Relative relative = layer3RelativeDh.relative();
                  if (relative.p().p1() < relative.p().p2mp1()) {
                    yield misfit(layer3RelativeDh, measurement(), measurement().hDiff());
                  }
                  else {
                    yield Double.POSITIVE_INFINITY;
                  }
                }
                case Model.Layer3AbsoluteDh(Model.Layer3Absolute layer3Absolute, Model.P dp) -> {
                  if (layer3Absolute.p().p1() < layer3Absolute.p().p2mp1()) {
                    yield misfitLog(layer3Absolute, layer3Absolute.toModel(dp), measurement());
                  }
                  else {
                    yield Double.POSITIVE_INFINITY;
                  }
                }
                default -> throw new IllegalArgumentException("Unexpected value: " + layer);
              };
        }

        @Override
        public ToDoubleFunction<Model> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer ->
                switch (Objects.requireNonNull(layer)) {
                  case Model.Layer2Relative(K k, double h) -> regularization2(k, h);
                  case Model.Layer3RelativeDh(Model.Layer3Relative layer3Relative, _) ->
                      regularization3(layer3Relative);
                  case Model.Layer3AbsoluteDh(Model.Layer3Absolute layer3Absolute, _) ->
                      regularization3(layer3Absolute);
                  default -> throw new IllegalArgumentException("Unexpected value: " + layer);
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
        public ToDoubleFunction<Model> misfit() {
          return layer -> {
            if (Objects.requireNonNull(layer) instanceof Model.Layer2DhModel relative) {
              return misfit(relative, measurement(), relative.dh());
            }
            throw new IllegalArgumentException("Unexpected value: " + layer);
          };
        }
      }

      private static final class MaxDiffRelative extends AbstractDiffRelative<TetrapolarMeasurement.MaxDiff> {
        private MaxDiffRelative(ElectrodeSystem.Inexact system, TetrapolarMeasurement.MaxDiff measurement) {
          super(system, measurement);
        }

        @Override
        public ToDoubleFunction<Model> misfit() {
          return layer -> {
            if (Objects.requireNonNull(layer) instanceof Model.Layer2DhModel relative) {
              return misfit(relative, measurement(), relative.dh());
            }
            else {
              throw new IllegalArgumentException("Unexpected value: " + layer);
            }
          };
        }

        @Override
        public ToDoubleFunction<Model> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer -> {
              if (Objects.requireNonNull(layer) instanceof Model.Layer2DhModel layer2Relative) {
                return regularization2(layer2Relative.relative().k(), layer2Relative.relative().h()) +
                    Numbers.regularization(Math.abs(layer2Relative.dh()), 0, 2.0 * Math.abs(measurement().hDiffMax()));
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
                return regularization2(layer2Relative.k(), layer2Relative.h()) +
                    Numbers.regularization(dh, -Math.abs(measurement().hDiffZero()), Math.abs(measurement().hDiffZero()));
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
            if (Objects.requireNonNull(layer) instanceof Model.Layer3RelativeDh model) {
              Model.Layer3Relative relative = model.relative();
              Model.P dp = model.dp();
              if (relative.p().p1() < relative.p().p2mp1() && dp.p1() < dp.p2mp1()) {
                return DoubleStream.of(
                    misfit(model, measurement(), measurement().hDiffMax()),
                    misfit(model, measurement().next(), measurement().next().hDiffMax())
                ).reduce(StrictMath::hypot).orElseThrow();
              }
              else {
                return Double.POSITIVE_INFINITY;
              }
            }
            throw new IllegalArgumentException("Unexpected value: " + layer);
          };
        }

        @Override
        public ToDoubleFunction<Model> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer -> {
              if (Objects.requireNonNull(layer) instanceof Model.Layer3Model layer3Model) {
                return regularization3(layer3Model);
              }
              else {
                throw new IllegalArgumentException("Unexpected value: " + layer);
              }
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
