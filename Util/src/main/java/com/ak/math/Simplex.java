package com.ak.math;

import io.jenetics.*;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.Limits;
import io.jenetics.util.DoubleRange;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.util.Pair;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.stream.IntStream;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;

public enum Simplex {
  NELDER_MEAD {
    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    PointValuePair optimize(MultivariateFunction function, Bounds... bounds) {
      return optimize(point -> {
        for (var i = 0; i < point.length; i++) {
          if (bounds[i].min > point[i] || bounds[i].max < point[i]) {
            return Double.POSITIVE_INFINITY;
          }
        }
        return function.value(point);
      }, Simplex.toInitialGuess(bounds), Simplex.toInitialSteps(bounds));
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    private static PointValuePair optimize(MultivariateFunction function, double[] initialGuess, double[] initialSteps) {
      return new SimplexOptimizer(STOP_FITNESS, STOP_FITNESS)
          .optimize(
              new MaxEval(MAX_ITERATIONS),
              new ObjectiveFunction(function), GoalType.MINIMIZE,
              new NelderMeadSimplex(initialSteps), new InitialGuess(initialGuess)
          );
    }

  },
  CMAES {
    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    PointValuePair optimize(MultivariateFunction function, Bounds... bounds) {
      SimpleBounds simpleBounds = new SimpleBounds(
          Arrays.stream(bounds).mapToDouble(Bounds::min).toArray(),
          Arrays.stream(bounds).mapToDouble(Bounds::max).toArray()
      );
      try {
        return new CMAESOptimizer(MAX_ITERATIONS, STOP_FITNESS, true, 0,
            10, new MersenneTwister(), false, null)
            .optimize(
                new MaxEval(MAX_ITERATIONS),
                new ObjectiveFunction(function),
                GoalType.MINIMIZE,
                new InitialGuess(Simplex.toInitialGuess(bounds)),
                simpleBounds,
                new CMAESOptimizer.Sigma(Simplex.toInitialSteps(bounds)),
                new CMAESOptimizer.PopulationSize(4 + (int) (3.0 * StrictMath.log(bounds.length)))
            );
      }
      catch (Exception e) {
        var nan = new double[bounds.length];
        Arrays.fill(nan, Double.NaN);
        return new PointValuePair(nan, Double.NaN);
      }
    }
  },
  JENETICS {
    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    PointValuePair optimize(MultivariateFunction function, Bounds... bounds) {
      int populationSize = 1 << (2 * bounds.length);

      if (Arrays.stream(bounds).noneMatch(b -> Double.isNaN(b.initialGuess))) {
        populationSize = 1 << (6 + bounds.length);
      }

      Phenotype<DoubleGene, Double> phenotype = Engine
          .builder(function::value,
              Codecs.ofVector(Arrays.stream(bounds).map(b -> DoubleRange.of(b.min, b.max)).toArray(DoubleRange[]::new))
          )
          .populationSize(populationSize)
          .optimize(Optimize.MINIMUM)
          .alterers(new Mutator<>(0.03), new MeanAlterer<>(0.6))
          .build().stream()
          .limit(Limits.bySteadyFitness(7))
          .limit(100)
          .collect(toBestPhenotype());
      Genotype<DoubleGene> best = phenotype.genotype();
      double[] point = IntStream.range(0, best.length()).mapToDouble(i -> best.get(i).get(0).doubleValue()).toArray();
      return new PointValuePair(point, phenotype.fitness());
    }
  };

  public record Bounds(double min, double initialGuess, double max) {
    public Bounds(double min, double initialGuess, double max) {
      this.min = Math.min(min, max);
      this.initialGuess = Math.min(Math.max(min, initialGuess), max);
      this.max = Math.max(min, max);
    }

    public Bounds(double min, double max) {
      this(min, Double.NaN, max);
    }

    public boolean isIn(double d) {
      return min < d && d < max;
    }
  }

  private static final double STOP_FITNESS = 1.0e-10;
  private static final int MAX_ITERATIONS = 300000;

  @Nonnull
  @ParametersAreNonnullByDefault
  abstract PointValuePair optimize(MultivariateFunction function, Bounds... bounds);

  @ParametersAreNonnullByDefault
  public static PointValuePair optimizeAll(MultivariateFunction function, Bounds... bounds) {
    PointValuePair optimize = JENETICS.optimize(function, bounds);
    if (Double.isFinite(optimize.getValue())) {
      double[] initialGuess = optimize.getPoint();
      Bounds[] minInitialMax = IntStream.range(0, initialGuess.length)
          .mapToObj(i -> new Bounds(bounds[i].min, initialGuess[i], bounds[i].max))
          .toArray(Bounds[]::new);

      return EnumSet.complementOf(EnumSet.of(JENETICS)).stream()
          .map(simplex -> simplex.optimize(function, minInitialMax))
          .min(Comparator.comparingDouble(Pair::getValue)).orElseThrow();
    }
    return optimize;
  }

  @Nonnull
  private static double[] toInitialSteps(@Nonnull Bounds[] bounds) {
    return Arrays.stream(bounds).mapToDouble(b -> Math.abs((b.max - b.min) / 100.0)).toArray();
  }

  @Nonnull
  private static double[] toInitialGuess(@Nonnull Bounds[] bounds) {
    return Arrays.stream(bounds).mapToDouble(Bounds::initialGuess).toArray();
  }
}