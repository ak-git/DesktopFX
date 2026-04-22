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
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public sealed interface Solver {
  static <M extends TetrapolarMeasurement> Step1<M> of(double base, Metrics.Length units) {
    return new SolverBuilder<>(base, units);
  }

  sealed interface Step1<M extends TetrapolarMeasurement> {
    Step2<M> system1x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);

    Step2<M> system1x2(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);
  }

  sealed interface Step2<M extends TetrapolarMeasurement> {
    Builder<Solver> system5x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);

    Builder<Solver> system1x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);
  }

  final class SolverBuilder<M extends TetrapolarMeasurement> implements Step1<M>, Step2<M>, Builder<Solver> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolverBuilder.class);

    private record SolverRecord() implements Solver {
    }

    private final double base;
    private final Metrics.Length units;
    private final Collection<ParametricOperator> parametricOperators = new ArrayList<>();

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
    public Step2<M> system1x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      system(1, 3, builderFunction);
      return this;
    }

    @Override
    public Step2<M> system1x2(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      system(1, 2, builderFunction);
      return this;
    }

    @Override
    public Builder<Solver> system5x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      system(5, 3, builderFunction);
      return this;
    }

    @Override
    public Builder<Solver> system1x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      system(1, 4, builderFunction);
      return this;
    }

    private void system(int factorFirst, int factorLast, Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      parametricOperators.add(
          ParametricOperator.builder(units)
              .system(s -> s.tetrapolar(base * factorFirst, base * factorLast).absError(0.1))
              .measurements(_ -> builderFunction.apply(TetrapolarMeasurement.builder()))
              .build()
      );
    }

    @Override
    public Solver build() {
      double dataErrorNorm = parametricOperators.stream().mapToDouble(ParametricOperator::dataErrorNorm).reduce(Math::hypot).orElseThrow();
      LOGGER.atInfo().addKeyValue("data Error Norm", "%.4f".formatted(dataErrorNorm)).log(Strings.EMPTY);

      DoubleFunction<Model.Layer2Relative> find = alpha -> {
        PointValuePair optimized = Simplex.optimizeAll(point -> {
              Model.Layer2Relative m = new Model.Layer2Relative(point[0], point[1]);
              return DoubleStream.concat(
                      parametricOperators.stream().mapToDouble(f -> alpha * f.regularization(ParametricOperator.Regularization.ZERO_MAX_LOG).applyAsDouble(m)),
                      parametricOperators.stream().mapToDouble(f -> f.misfit().applyAsDouble(m)).map(x -> x * x)
                  )
                  .takeWhile(Double::isFinite).boxed().collect(
                      Collectors.teeing(
                          Collectors.reducing(Double::sum),
                          Collectors.counting(),
                          (sum, count) -> count == parametricOperators.size() * 2L ? sum.orElseThrow() : Double.POSITIVE_INFINITY
                      )
                  );
            },
            new Simplex.Bounds(-1.0, 1.0), new Simplex.Bounds(0.0, parametricOperators.stream().mapToDouble(ParametricOperator::hMax).min().orElseThrow()));
        double[] point = optimized.getPoint();
        return new Model.Layer2Relative(point[0], point[1]);
      };

      DoubleUnaryOperator withAlpha = new DoubleUnaryOperator() {
        private final double dataErrorNorm = parametricOperators.stream().mapToDouble(ParametricOperator::dataErrorNorm).reduce(Math::hypot).orElseThrow();

        @Override
        public double applyAsDouble(double alpha) {
          if (alpha < 0) {
            return Double.POSITIVE_INFINITY;
          }
          else {
            Model.Layer2Relative m = find.apply(alpha);
            double misfit = parametricOperators.stream().mapToDouble(f -> f.misfit().applyAsDouble(m)).reduce(Math::hypot).orElseThrow();
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
