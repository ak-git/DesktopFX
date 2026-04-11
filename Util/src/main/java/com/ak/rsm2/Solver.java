package com.ak.rsm2;

import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.util.Builder;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.math4.legacy.optim.InitialGuess;
import org.apache.commons.math4.legacy.optim.MaxEval;
import org.apache.commons.math4.legacy.optim.PointValuePair;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.NelderMeadTransform;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

public sealed interface Solver {
  static Step1 of(double base, Metrics.Length units) {
    return new SolverBuilder(base, units);
  }

  sealed interface Step1 {
    Step2 system1x3(Function<TetrapolarMeasurement.Step1, Builder<TetrapolarMeasurement>> builderFunction);
  }

  sealed interface Step2 {
    Builder<Solver> system5x3(Function<TetrapolarMeasurement.Step1, Builder<TetrapolarMeasurement>> builderFunction);
  }

  final class SolverBuilder implements Step1, Step2, Builder<Solver> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolverBuilder.class);

    private record SolverRecord() implements Solver {
    }

    private final double base;
    private final Metrics.Length units;
    private final Collection<Misfit> misfits = new ArrayList<>();

    public SolverBuilder(double base, Metrics.Length units) {
      if (base > 0) {
        this.base = base;
      }
      else {
        throw new IllegalArgumentException("base = %f must be positive".formatted(base));
      }
      this.units = units;
    }

    @Override
    public Step2 system1x3(Function<TetrapolarMeasurement.Step1, Builder<TetrapolarMeasurement>> builderFunction) {
      systemX3(1, builderFunction);
      return this;
    }

    @Override
    public Builder<Solver> system5x3(Function<TetrapolarMeasurement.Step1, Builder<TetrapolarMeasurement>> builderFunction) {
      systemX3(5, builderFunction);
      return this;
    }

    private void systemX3(int factorFirst, Function<TetrapolarMeasurement.Step1, Builder<TetrapolarMeasurement>> builderFunction) {
      misfits.add(
          Misfit.builder(units)
              .system(s -> s.tetrapolar(base * factorFirst, base * 3).absError(0.1))
              .measurements(_ -> builderFunction.apply(TetrapolarMeasurement.builder(units)))
              .build()
      );
    }

    @Override
    public Solver build() {
      double dataErrorNorm = misfits.stream().mapToDouble(Misfit::dataErrorNorm).reduce(Math::hypot).orElseThrow();
      LOGGER.atInfo().addKeyValue("data Error Norm", "%.4f".formatted(dataErrorNorm)).log(Strings.EMPTY);

      DoubleFunction<Model.Layer2Relative> find = alpha -> {
        PointValuePair optimized = Simplex.optimizeAll(point -> {
              Model.Layer2Relative m = new Model.Layer2Relative(point[0], point[1]);
              double s = misfits.stream().mapToDouble(f -> f.regularization(Misfit.Regularization.ZERO_MAX_LOG).applyAsDouble(m)).sum();
              if (Double.isFinite(s)) {
                double misfit = misfits.stream().mapToDouble(f -> f.misfit().applyAsDouble(m)).reduce(Math::hypot).orElseThrow();
                return misfit * misfit + Math.abs(alpha) * s;
              }
              else {
                return Double.POSITIVE_INFINITY;
              }
            },
            new Simplex.Bounds(-1.0, 1.0), new Simplex.Bounds(0.0, misfits.stream().mapToDouble(Misfit::hMax).min().orElseThrow()));
        double[] point = optimized.getPoint();
        return new Model.Layer2Relative(point[0], point[1]);
      };

      DoubleUnaryOperator withAlpha = new DoubleUnaryOperator() {
        private final double dataErrorNorm = misfits.stream().mapToDouble(Misfit::dataErrorNorm).reduce(Math::hypot).orElseThrow();

        @Override
        public double applyAsDouble(double alpha) {
          if (alpha < 0) {
            return Double.POSITIVE_INFINITY;
          }
          else {
            Model.Layer2Relative m = find.apply(alpha);
            double misfit = misfits.stream().mapToDouble(f -> f.misfit().applyAsDouble(m)).reduce(Math::hypot).orElseThrow();
            LOGGER.atInfo().addKeyValue("alpha", "%.4f".formatted(alpha)).addKeyValue("misfit", "%.4f".formatted(misfit))
                .log(() -> "%s; %s".formatted(
                    ValuePair.Name.K12.of(find.apply(alpha).k().value(), 0.0),
                    ValuePair.Name.H.of(find.apply(alpha).h(), 0.0))
                );
            double v = misfit - dataErrorNorm;
            return v * v;
          }
        }
      };

      PointValuePair optimized = new SimplexOptimizer(0.000_000_1, 0.000_01)
          .optimize(new MaxEval(100), new ObjectiveFunction(point -> withAlpha.applyAsDouble(point[0])),
              GoalType.MINIMIZE, org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.Simplex.alongAxes(new double[] {0.001}),
              new NelderMeadTransform(), new InitialGuess(new double[] {0.0})
          );
      double alpha = optimized.getPoint()[0];
      LOGGER.atWarn().addKeyValue("alpha", () -> "%.4f".formatted(alpha))
          .log("%s; %s".formatted(
              ValuePair.Name.K12.of(find.apply(alpha).k().value(), 0.0),
              ValuePair.Name.H.of(find.apply(alpha).h(), 0.0)));

      return new SolverRecord();
    }
  }
}
