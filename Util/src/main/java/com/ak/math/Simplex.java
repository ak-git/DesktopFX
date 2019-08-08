package com.ak.math;

import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
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
}