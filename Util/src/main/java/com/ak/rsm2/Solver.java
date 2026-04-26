package com.ak.rsm2;

import com.ak.math.Simplex;
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
  static <M extends TetrapolarMeasurement> Step1<M> of(double base, Metrics.Length units, Function<double[], Model> modelFactory) {
    return new SolverBuilder<>(base, units, modelFactory);
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
    private final Function<double[], Model> modelFactory;
    private final Collection<ParametricFunctional> parametricFunctionals = new ArrayList<>();

    public SolverBuilder(double base, Metrics.Length units, Function<double[], Model> modelFactory) {
      if (base > 0) {
        this.base = base;
      }
      else {
        throw new IllegalArgumentException("base = %f must be positive".formatted(base));
      }
      this.units = units;
      this.modelFactory = modelFactory;
    }

    @Override
    public Step2<M> system1x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(1, 3, builderFunction);
      return this;
    }

    @Override
    public Step2<M> system1x2(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(1, 2, builderFunction);
      return this;
    }

    @Override
    public Builder<Solver> system5x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(5, 3, builderFunction);
      return this;
    }

    @Override
    public Builder<Solver> system1x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(1, 4, builderFunction);
      return this;
    }

    private void addSystem(int factorFirst, int factorLast, Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      parametricFunctionals.add(
          ParametricFunctional.builder(units)
              .system(s -> s.tetrapolar(base * factorFirst, base * factorLast).absError(0.1))
              .measurements(_ -> builderFunction.apply(TetrapolarMeasurement.builder()))
              .build()
      );
    }

    @Override
    public Solver build() {
      double dataErrorNorm = parametricFunctionals.stream().mapToDouble(ParametricFunctional::dataErrorNorm).reduce(Math::hypot).orElseThrow();
      LOGGER.atInfo().addKeyValue("data Error Norm", "%.4f".formatted(dataErrorNorm)).log(Strings.EMPTY);

      DoubleFunction<Model> find = alpha -> {
        PointValuePair optimized = Simplex.optimizeAll(point -> {
              Model m = modelFactory.apply(point);
              return DoubleStream.concat(
                      parametricFunctionals.stream().mapToDouble(f -> alpha * f.regularization(ParametricFunctional.Regularization.ZERO_MAX_LOG).applyAsDouble(m)),
                      parametricFunctionals.stream().mapToDouble(f -> f.misfit().applyAsDouble(m)).map(x -> x * x)
                  )
                  .takeWhile(Double::isFinite).boxed().collect(
                      Collectors.teeing(
                          Collectors.reducing(Double::sum),
                          Collectors.counting(),
                          (sum, count) -> count == parametricFunctionals.size() * 2L ? sum.orElseThrow() : Double.POSITIVE_INFINITY
                      )
                  );
            },
            parametricFunctionals.stream().map(ParametricFunctional::bounds).reduce((bounds1, bounds2) -> {
              Simplex.Bounds[] bounds = new Simplex.Bounds[Math.max(bounds1.length, bounds2.length)];
              for (int i = 0; i < bounds.length; i++) {
                bounds[i] = bounds1[i].merge(bounds2[i]);
              }
              return bounds;
            }).orElseThrow());
        return modelFactory.apply(optimized.getPoint());
      };

      DoubleUnaryOperator withAlpha = new DoubleUnaryOperator() {
        private final double dataErrorNorm = parametricFunctionals.stream().mapToDouble(ParametricFunctional::dataErrorNorm).reduce(Math::hypot).orElseThrow();

        @Override
        public double applyAsDouble(double alpha) {
          if (alpha < 0) {
            return Double.POSITIVE_INFINITY;
          }
          else {
            Model m = find.apply(alpha);
            double misfit = parametricFunctionals.stream().mapToDouble(f -> f.misfit().applyAsDouble(m)).reduce(Math::hypot).orElseThrow();
            LOGGER.atInfo().addKeyValue("alpha", "%.4f".formatted(alpha)).addKeyValue("misfit", "%.4f".formatted(misfit))
                .log(() -> "%s".formatted(find.apply(alpha)));
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
      LOGGER.atWarn().addKeyValue("alpha", () -> "%.4f".formatted(alpha)).log("%s".formatted(find.apply(alpha)));
      return new SolverRecord();
    }
  }
}
