package com.ak.math;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.random.MersenneTwister;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.pow;
import static org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer.DEFAULT_STOPPING_RADIUS;

public class SimplexTest {
  private SimplexTest() {
  }

  @Test(timeOut = 10000)
  public static void testRosenbrockNelderMeadSimplex() {
    PointValuePair optimum = optimizeNelderMead(new Rosenbrock(), new double[] {0.0, 0.0}, new double[] {0.1, 0.1});
    Assert.assertTrue(optimum.getValue() < 1.0e-6);
    Assert.assertEquals(optimum.getPoint()[0], 1.0, 1.0e-5);
  }

  private static PointValuePair optimizeNelderMead(@Nonnull MultivariateFunction function,
                                                   @Nonnull double[] initialGuess, @Nonnull double[] initialSteps) {
    return new SimplexOptimizer(-1, 1.0e-8).optimize(new MaxEval(10000), new ObjectiveFunction(function), GoalType.MINIMIZE,
        new NelderMeadSimplex(initialSteps), new InitialGuess(initialGuess));
  }

  public static PointValuePair optimizeCMAES(@Nonnull MultivariateFunction function, @Nonnull SimpleBounds bounds,
                                             @Nonnull double[] initialGuess, @Nonnull double[] initialSteps) {
    return new CMAESOptimizer(30000, 1.0e-11, true, 0,
        10, new MersenneTwister(), false, null)
        .optimize(
            new MaxEval(30000),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(initialGuess),
            bounds,
            new CMAESOptimizer.Sigma(initialSteps),
            new CMAESOptimizer.PopulationSize(2 * (4 + (int) (3.0 * StrictMath.log(initialGuess.length))))
        );
  }

  public static PointValuePair optimizeBOBYQA(@Nonnull MultivariateFunction function, @Nonnull SimpleBounds bounds,
                                              @Nonnull double[] initialGuess, @Nonnegative double initialRadius) {
    return new BOBYQAOptimizer(2 * initialGuess.length + 1, initialRadius, DEFAULT_STOPPING_RADIUS)
        .optimize(
            new MaxEval(30000),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(initialGuess),
            bounds
        );
  }

  @Test(invocationCount = 10)
  public void testConstrainedRosenbrockCMAESOptimizer() {
    int DIM = 2;
    double[] startPoint = point(DIM, 0.1);
    double[] inSigma = point(DIM, 0.1);
    double[][] boundaries = boundaries(DIM, -(Math.random() + 0.5), 2.0 * (Math.random() + 0.5));
    PointValuePair expected = new PointValuePair(point(DIM, 1.0), 0.0);

    PointValuePair result = optimizeCMAES(new Rosenbrock(), new SimpleBounds(boundaries[0], boundaries[1]), startPoint, inSigma);

    Logger.getAnonymousLogger().finest("sol=" + Arrays.toString(result.getPoint()));
    Assert.assertEquals(expected.getValue(), result.getValue(), 1.0e-10);
    for (int i = 0; i < DIM; i++) {
      Assert.assertEquals(expected.getPoint()[i], result.getPoint()[i], 1.0e-5);
    }
  }

  @Test
  public void testConstrainedRosenbrockBOBYQAOptimizer() {
    int DIM = 2;
    double[] startPoint = point(DIM, 0.1);
    double[][] boundaries = boundaries(DIM, -(Math.random() + 0.5), 2.0 * (Math.random() + 0.5));
    PointValuePair expected = new PointValuePair(point(DIM, 1.0), 0.0);

    PointValuePair result = optimizeBOBYQA(new Rosenbrock(), new SimpleBounds(boundaries[0], boundaries[1]), startPoint, 0.1);

    Logger.getAnonymousLogger().finest("sol=" + Arrays.toString(result.getPoint()));
    Assert.assertEquals(expected.getValue(), result.getValue(), 1.0e-13);
    for (int i = 0; i < DIM; i++) {
      Assert.assertEquals(expected.getPoint()[i], result.getPoint()[i], 1.0e-6);
    }
  }

  private static double[] point(@Nonnegative int n, double value) {
    double[] ds = new double[n];
    Arrays.fill(ds, value);
    return ds;
  }

  private static double[][] boundaries(@Nonnegative int dim, double lower, double upper) {
    double[][] boundaries = new double[2][dim];
    for (int i = 0; i < dim; i++) {
      boundaries[0][i] = lower;
    }
    for (int i = 0; i < dim; i++) {
      boundaries[1][i] = upper;
    }
    return boundaries;
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
