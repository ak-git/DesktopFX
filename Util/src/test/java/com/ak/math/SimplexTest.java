package com.ak.math;

import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.pow;

public class SimplexTest {
  private SimplexTest() {
  }

  @Test(timeOut = 10000)
  public static void testRosenbrockNelderMeadSimplex() {
    SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-3);
    PointValuePair optimum = optimizer.optimize(new MaxEval(100), new ObjectiveFunction(new Rosenbrock()),
        GoalType.MINIMIZE, new NelderMeadSimplex(2, 0.1), new InitialGuess(new double[] {0.0, 0.0})
    );

    Assert.assertEquals(optimizer.getEvaluations(), 40);
    Assert.assertTrue(optimum.getValue() < 1.0e-3);
  }

  private static class Rosenbrock implements MultivariateFunction {
    @Override
    public double value(double[] x) {
      double a = pow(x[0], 2.0) - x[1];
      double b = x[0] - 1.0;
      return 10 * pow(a, 2.0) + pow(b, 2.0);
    }
  }

  @Test(enabled = false)
  public static void testLinearSolve() {
    double[][] A = {
        {0.17, 0.44},
        {0.64, 0.79}
    };

    double[] B = {0, 6.9};
    Logger.getAnonymousLogger().info(Arrays.toString(new LUDecomposition(new Array2DRowRealMatrix(A)).getSolver().solve(new ArrayRealVector(B)).toArray()));
  }
}
