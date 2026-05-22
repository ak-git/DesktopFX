package com.ak.rsm2;

import com.ak.math.Simplex;
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

  Simplex.Bounds[] bounds();

  ToDoubleFunction<IterativeModel> misfit();

  ToDoubleFunction<IterativeModel> regularization(Regularization regularization);

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

      protected final double misfit(Model model, double dh) {
        Resistivity.Apparent resistivity = Resistivity.of(system).apparentDivRho1(model);
        double apparent = resistivity.apparent(measurement.ohms());
        double derivativeApparentByPhi = resistivity.apparent((measurement.ohmsDiff() / dh) / system.phiFactor());
        double v = log(resistivity.value() / apparent) - log(resistivity.derivativeByPhi() / derivativeApparentByPhi);
        return Double.isNaN(v) ? Double.POSITIVE_INFINITY : Math.abs(v);
      }

      protected static double regularization(double dh, double dhMax) {
        double x = Math.abs(dh);
        double max = Math.abs(dhMax);
        double s = log(2.0 * max - x) - log(x);
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
        public Simplex.Bounds[] bounds() {
          return new Simplex.Bounds[] {
              new Simplex.Bounds(-1.0, 1.0), new Simplex.Bounds(0.0, system().hMax(K.PLUS_ONE))
          };
        }

        @Override
        public ToDoubleFunction<IterativeModel> misfit() {
          return layer -> {
            switch (layer) {
              case IterativeModel.Layer2Relative layer2Relative -> {
                return misfit(layer2Relative.toModel(), measurement().hDiff());
              }
              case IterativeModel.Layer2RelativeDh layer2RelativeDH ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2RelativeDH);
              case IterativeModel.Layer3Relative layer3Relative ->
                  throw new IllegalArgumentException("Unexpected value: " + layer3Relative);
            }
          };
        }

        @Override
        public ToDoubleFunction<IterativeModel> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer ->
                switch (layer) {
                  case IterativeModel.Layer2Relative(K k, double h) -> regularization(k, h);
                  case IterativeModel.Layer2RelativeDh layer2RelativeDH ->
                      throw new IllegalArgumentException("Unexpected value: " + layer2RelativeDH);
                  case IterativeModel.Layer3Relative layer3Relative ->
                      throw new IllegalArgumentException("Unexpected value: " + layer3Relative);
                };
          };
        }
      }

      private static final class MaxDiffRelative extends AbstractParametricFunctional<TetrapolarMeasurement.MaxDiff> {
        private MaxDiffRelative(ElectrodeSystem.Inexact system, TetrapolarMeasurement.MaxDiff measurement) {
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
        public ToDoubleFunction<IterativeModel> misfit() {
          return layer -> {
            switch (layer) {
              case IterativeModel.Layer2Relative layer2Relative ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2Relative);
              case IterativeModel.Layer2RelativeDh layer2RelativeDH -> {
                return misfit(layer2RelativeDH.toModel(), layer2RelativeDH.dh());
              }
              case IterativeModel.Layer3Relative layer3Relative ->
                  throw new IllegalArgumentException("Unexpected value: " + layer3Relative);
            }
          };
        }

        @Override
        public ToDoubleFunction<IterativeModel> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer ->
                switch (layer) {
                  case IterativeModel.Layer2Relative layer2Relative ->
                      throw new IllegalArgumentException("Unexpected value: " + layer2Relative);
                  case IterativeModel.Layer2RelativeDh(K k, double h, double dh) ->
                      regularization(k, h) + regularization(dh, measurement().hDiffMax());
                  case IterativeModel.Layer3Relative layer3Relative ->
                      throw new IllegalArgumentException("Unexpected value: " + layer3Relative);
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
        public Simplex.Bounds[] bounds() {
          double hMax = system().hMax(K.PLUS_ONE);
          double hDiffPos = Math.max(measurement().hDiffMax(), measurement().next().hDiffMax());
          double hDiffNeg = Math.min(measurement().hDiffMax(), measurement().next().hDiffMax());
          Simplex.Bounds pDiffBounds = new Simplex.Bounds(Math.min(hDiffNeg, 0.0), Math.max(0.0, hDiffPos));
          return new Simplex.Bounds[] {
              new Simplex.Bounds(0.0, 1.0),
              new Simplex.Bounds(-1.0, 0.0),
              new Simplex.Bounds(0.0, hMax),
              new Simplex.Bounds(0.0, hMax),
              pDiffBounds, pDiffBounds, pDiffBounds
          };
        }

        @Override
        public ToDoubleFunction<IterativeModel> misfit() {
          return layer -> {
            switch (layer) {
              case IterativeModel.Layer2Relative layer2Relative ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2Relative);
              case IterativeModel.Layer2RelativeDh layer2RelativeDH ->
                  throw new IllegalArgumentException("Unexpected value: " + layer2RelativeDH);
              case IterativeModel.Layer3Relative layer3Relative -> {
                return DoubleStream.of(
                        misfit(layer3Relative.toModel(), layer3Relative.dh()),
                        misfit(layer3Relative.toModelAfterFat(), layer3Relative.dh()),
                        misfit(layer3Relative.toModelDiffFat(), layer3Relative.dhDiffFat()))
                    .reduce(StrictMath::hypot).orElseThrow();
              }
            }
          };
        }

        @Override
        public ToDoubleFunction<IterativeModel> regularization(Regularization regularization) {
          return switch (regularization) {
            case ZERO_MAX_LOG -> layer ->
                switch (layer) {
                  case IterativeModel.Layer2Relative layer2Relative ->
                      throw new IllegalArgumentException("Unexpected value: " + layer2Relative);
                  case IterativeModel.Layer2RelativeDh layer2RelativeDH ->
                      throw new IllegalArgumentException("Unexpected value: " + layer2RelativeDH);
                  case IterativeModel.Layer3Relative _ -> 0.0;
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
        case TetrapolarMeasurement.Diff diff -> new AbstractParametricFunctional.Diff(s, diff);
        case TetrapolarMeasurement tetrapolarMeasurement ->
            throw new IllegalArgumentException("Unexpected value: " + tetrapolarMeasurement);
      };
    }
  }
}
