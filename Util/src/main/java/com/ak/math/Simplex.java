package com.ak.math;

import java.util.Comparator;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

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

public enum Simplex {
  ;

  private static final double STOP_FITNESS = 1.0e-10;
  private static final int MAX_ITERATIONS = 300000;

  public static PointValuePair optimizeCMAES(@Nonnull MultivariateFunction function, @Nonnull SimpleBounds bounds,
                                             @Nonnull double[] initialGuess, @Nonnull double[] initialSteps) {
    return IntStream.range(0, 2).mapToObj(value -> new CMAESOptimizer(MAX_ITERATIONS, STOP_FITNESS, true, 0,
        10, new MersenneTwister(), false, null)
        .optimize(
            new MaxEval(MAX_ITERATIONS),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(initialGuess),
            bounds,
            new CMAESOptimizer.Sigma(initialSteps),
            new CMAESOptimizer.PopulationSize(2 * (4 + (int) (3.0 * StrictMath.log(initialGuess.length))))
        )).parallel().min(Comparator.comparingDouble(Pair::getValue)).orElseThrow();
  }

  public static PointValuePair optimize(@Nonnull MultivariateFunction function, @Nonnull SimpleBounds bounds,
                                        @Nonnull double[] initialGuess, @Nonnull double[] initialSteps) {
    return optimize(point -> {
      for (int i = 0; i < point.length; i++) {
        if (bounds.getLower()[i] > point[i] || bounds.getUpper()[i] < point[i]) {
          return Double.POSITIVE_INFINITY;
        }
      }
      return function.value(point);
    }, initialGuess, initialSteps);
  }

  private static PointValuePair optimize(@Nonnull MultivariateFunction function,
                                         @Nonnull double[] initialGuess, @Nonnull double[] initialSteps) {
    return new SimplexOptimizer(STOP_FITNESS, STOP_FITNESS).optimize(new MaxEval(MAX_ITERATIONS), new ObjectiveFunction(function), GoalType.MINIMIZE,
        new NelderMeadSimplex(initialSteps), new InitialGuess(initialGuess));
  }
}