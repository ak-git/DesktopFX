package com.ak.math;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
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
    PointValuePair optimize(MultivariateFunction function, double[]... minInitialMax) {
      SimpleBounds bounds = Simplex.toBounds(minInitialMax);
      return optimize(point -> {
        for (var i = 0; i < point.length; i++) {
          if (bounds.getLower()[i] > point[i] || bounds.getUpper()[i] < point[i]) {
            return Double.POSITIVE_INFINITY;
          }
        }
        return function.value(point);
      }, Simplex.toInitialGuess(minInitialMax), Simplex.toInitialSteps(bounds));
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
    PointValuePair optimize(MultivariateFunction function, double[]... minInitialMax) {
      SimpleBounds bounds = Simplex.toBounds(minInitialMax);
      try {
        return new CMAESOptimizer(MAX_ITERATIONS, STOP_FITNESS, true, 0,
            10, new MersenneTwister(), false, null)
            .optimize(
                new MaxEval(MAX_ITERATIONS),
                new ObjectiveFunction(function),
                GoalType.MINIMIZE,
                new InitialGuess(Simplex.toInitialGuess(minInitialMax)),
                bounds,
                new CMAESOptimizer.Sigma(Simplex.toInitialSteps(bounds)),
                new CMAESOptimizer.PopulationSize(4 + (int) (3.0 * StrictMath.log(minInitialMax.length)))
            );
      }
      catch (Exception e) {
        var nan = new double[minInitialMax.length];
        Arrays.fill(nan, Double.NaN);
        return new PointValuePair(nan, Double.NaN);
      }
    }
  },
  JENETICS {
    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    PointValuePair optimize(MultivariateFunction function, double[]... minInitialMax) {
      Phenotype<DoubleGene, Double> phenotype = Engine
          .builder(function::value,
              Codecs.ofVector(
                  Arrays.stream(minInitialMax)
                      .map(initialMax -> DoubleRange.of(initialMax[0], initialMax[initialMax.length - 1]))
                      .toArray(DoubleRange[]::new)
              )
          )
          .populationSize(1 << (6 + minInitialMax.length))
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
  abstract PointValuePair optimize(MultivariateFunction function, double[]... minInitialMax);

  @ParametersAreNonnullByDefault
  public static PointValuePair optimizeAll(MultivariateFunction function, double[]... bounds) {
    double[] initialGuess = JENETICS.optimize(function, bounds).getPoint();
    double[][] minInitialMax = IntStream.range(0, initialGuess.length)
        .mapToObj(i -> new double[] {bounds[i][0], initialGuess[i], bounds[i][bounds[i].length - 1]})
        .toArray(double[][]::new);
    return EnumSet.complementOf(EnumSet.of(JENETICS)).stream()
        .map(simplex -> simplex.optimize(function, minInitialMax))
        .parallel().min(Comparator.comparingDouble(Pair::getValue)).orElseThrow();
  }

  @Nonnull
  private static double[] toInitialSteps(@Nonnull SimpleBounds bounds) {
    return IntStream.range(0, Math.max(bounds.getUpper().length, bounds.getLower().length))
        .mapToDouble(i -> Math.abs((bounds.getUpper()[i] - bounds.getLower()[i]) / 100.0))
        .toArray();
  }

  @Nonnull
  private static double[] toInitialGuess(@Nonnull double[][] minInitialMax) {
    return Arrays.stream(minInitialMax).mapToDouble(value -> value[1]).toArray();
  }

  @Nonnull
  private static SimpleBounds toBounds(@Nonnull double[][] minInitialMax) {
    if (Arrays.stream(minInitialMax).anyMatch(doubles -> doubles.length != 3)) {
      throw new IllegalArgumentException(Arrays.deepToString(minInitialMax));
    }
    return new SimpleBounds(
        Arrays.stream(minInitialMax).mapToDouble(value -> value[0]).toArray(),
        Arrays.stream(minInitialMax).mapToDouble(value -> value[value.length - 1]).toArray()
    );
  }
}