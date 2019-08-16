package com.ak.math;

import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

public class Simplex {
  private Simplex() {
  }

  public static PointValuePair optimizeNelderMead(@Nonnull MultivariateFunction function,
                                                  @Nonnull double[] initialGuess, @Nonnull double[] initialSteps) {
    return new SimplexOptimizer(1.0e-6, 1.0e-6).optimize(new MaxEval(30000), new ObjectiveFunction(function), GoalType.MINIMIZE,
        new NelderMeadSimplex(initialSteps), new InitialGuess(initialGuess));
  }

  public static PointValuePair optimizeNelderMead(@Nonnull MultivariateFunction function, @Nonnull SimpleBounds bounds,
                                                  @Nonnull double[] initialGuess, @Nonnull double[] initialSteps) {
    return optimizeNelderMead(point -> {
      for (int i = 0; i < point.length; i++) {
        if (bounds.getLower()[i] > point[i] && bounds.getUpper()[i] < point[i]) {
          return Double.POSITIVE_INFINITY;
        }
      }
      return function.value(point);
    }, initialGuess, initialSteps);
  }
}