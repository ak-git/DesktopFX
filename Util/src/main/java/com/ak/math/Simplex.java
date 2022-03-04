package com.ak.math;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.MeanAlterer;
import io.jenetics.Mutator;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
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

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;

public enum Simplex {
  NELDER_MEAD {
    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    PointValuePair optimize(MultivariateFunction function, SimpleBounds bounds, double[] initialGuess, double[] initialSteps) {
      return optimize(point -> {
        for (var i = 0; i < point.length; i++) {
          if (bounds.getLower()[i] > point[i] || bounds.getUpper()[i] < point[i]) {
            return Double.POSITIVE_INFINITY;
          }
        }
        return function.value(point);
      }, initialGuess, initialSteps);
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
    PointValuePair optimize(MultivariateFunction function, SimpleBounds bounds, double[] initialGuess, double[] initialSteps) {
      try {
        return new CMAESOptimizer(MAX_ITERATIONS, STOP_FITNESS, true, 0,
            10, new MersenneTwister(), false, null)
            .optimize(
                new MaxEval(MAX_ITERATIONS),
                new ObjectiveFunction(function),
                GoalType.MINIMIZE,
                new InitialGuess(initialGuess),
                bounds,
                new CMAESOptimizer.Sigma(DoubleStream.of(initialSteps).map(Math::abs).toArray()),
                new CMAESOptimizer.PopulationSize(4 + (int) (3.0 * StrictMath.log(initialGuess.length)))
            );
      }
      catch (Exception e) {
        var nan = new double[initialGuess.length];
        Arrays.fill(nan, Double.NaN);
        return new PointValuePair(nan, Double.NaN);
      }
    }
  },
  JENETICS {
    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    PointValuePair optimize(MultivariateFunction function, SimpleBounds bounds, double[] initialGuess, double[] initialSteps) {
      Phenotype<DoubleGene, Double> phenotype = Engine
          .builder(function::value,
              Codecs.ofVector(
                  IntStream.range(0, Math.min(initialGuess.length, initialSteps.length))
                      .mapToObj(i -> DoubleRange.of(bounds.getLower()[i], bounds.getUpper()[i]))
                      .toArray(DoubleRange[]::new)
              )
          )
          .populationSize(Math.max(256, 1 << Math.max(0, (initialGuess.length - 1) * 2)))
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

  private static final double STOP_FITNESS = 1.0e-10;
  private static final int MAX_ITERATIONS = 300000;

  @Nonnull
  @ParametersAreNonnullByDefault
  abstract PointValuePair optimize(MultivariateFunction function, SimpleBounds bounds,
                                   double[] initialGuess, double[] initialSteps);

  @ParametersAreNonnullByDefault
  public static PointValuePair optimizeAll(MultivariateFunction function, SimpleBounds bounds, double[] initialSteps) {
    double[] initialGuess = JENETICS.optimize(function, bounds, initialSteps, initialSteps).getPoint();
    return EnumSet.complementOf(EnumSet.of(JENETICS)).stream()
        .map(simplex -> simplex.optimize(function, bounds, initialGuess, initialSteps))
        .parallel().min(Comparator.comparingDouble(Pair::getValue)).orElseThrow();
  }
}