package com.ak.math;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SimplexTest {
  private static class Rosen implements MultivariateFunction {

    @Override
    public double value(double[] x) {
      double f = 0;
      for (int i = 0; i < x.length - 1; ++i) {
        f += 100.0 * (x[i] * x[i] - x[i + 1]) * (x[i] * x[i] - x[i + 1]) + (x[i] - 1.0) * (x[i] - 1.0);
      }
      return f;
    }
  }

  @Test
  public void testOptimizeCMAES() {
    PointValuePair valuePair = Simplex.CMAES.optimize(new Rosen(),
        new double[] {-10.0, 0.0, 10.0}, new double[] {-10.0, 0.0, 10.0});
    Assert.assertEquals(valuePair.getPoint(), new double[] {1.0, 1.0}, 0.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalid() {
    Simplex.CMAES.optimize(new Rosen(),
        new double[] {-10.0, 0.0, 10.0}, new double[] {-10.0, 0.0, 10.0}, new double[] {0.0});
  }
}