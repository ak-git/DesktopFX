package com.ak.math;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.random.MersenneTwister;
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

  @Test(invocationCount = 10)
  public void testRosenbrockCMAESOptimizer() {
    int DIM = 2;
    double[] startPoint = point(DIM, 0.1);
    double[] inSigma = point(DIM, 0.1);
    PointValuePair expected = new PointValuePair(point(DIM, 1.0), 0.0);
    doTest(new Rosenbrock(), startPoint, inSigma, null, expected);
  }

  @Test(invocationCount = 10)
  public void testConstrainedRosenbrockCMAESOptimizer() {
    int DIM = 2;
    double[] startPoint = point(DIM, 0.1);
    double[] inSigma = point(DIM, 0.1);
    double[][] boundaries = boundaries(DIM, -(Math.random() + 0.5), 2.0 * (Math.random() + 0.5));
    PointValuePair expected = new PointValuePair(point(DIM, 1.0), 0.0);
    doTest(new Rosenbrock(), startPoint, inSigma, boundaries, expected);
  }

  /**
   * @param func       Function to optimize.
   * @param startPoint Starting point.
   * @param inSigma    Individual input sigma.
   * @param boundaries Upper / lower point limit.
   * @param expected   Expected point / value.
   */
  private static void doTest(@Nonnull MultivariateFunction func,
                             @Nonnull double[] startPoint,
                             @Nonnull double[] inSigma,
                             @Nullable double[][] boundaries,
                             @Nonnull PointValuePair expected) {
    int dim = startPoint.length;
    int lambda = 2 * (4 + (int) (3.0 * StrictMath.log(dim)));
    // test diagonalOnly = 0 - slow but normally fewer feval#
    CMAESOptimizer optim = new CMAESOptimizer(30000, 1.0E-13, true, 0,
        0, new MersenneTwister(), false, null);

    SimpleBounds simpleBounds = SimpleBounds.unbounded(dim);
    if (boundaries != null) {
      simpleBounds = new SimpleBounds(boundaries[0], boundaries[1]);
    }
    PointValuePair result = optim.optimize(new MaxEval(100000),
        new ObjectiveFunction(func),
        GoalType.MINIMIZE,
        new InitialGuess(startPoint),
        simpleBounds,
        new CMAESOptimizer.Sigma(inSigma),
        new CMAESOptimizer.PopulationSize(lambda));

    Logger.getAnonymousLogger().finest("sol=" + Arrays.toString(result.getPoint()));
    Assert.assertEquals(expected.getValue(), result.getValue(), 1.0E-13);
    for (int i = 0; i < dim; i++) {
      Assert.assertEquals(expected.getPoint()[i], result.getPoint()[i], 1.0E-6);
    }

    Assert.assertTrue(optim.getIterations() > 0);
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
