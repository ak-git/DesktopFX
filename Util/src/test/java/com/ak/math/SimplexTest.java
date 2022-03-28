package com.ak.math;

import java.util.Arrays;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex.Bounds;
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
  public void testOptimizeWithInitialGuessAndBounds() {
    PointValuePair valuePair = Simplex.optimizeAll(new Rosen(),
        new Bounds(10.0, 20.0, 30.0), new Bounds(10.0, 20.0, 30.0));
    Assert.assertTrue(
        Arrays.stream(valuePair.getPoint()).anyMatch(value -> Inequality.absolute().applyAsDouble(value, 10.0) < 0.1),
        Arrays.toString(valuePair.getPoint())
    );
  }

  @Test
  public void testOptimizeWithBounds() {
    PointValuePair valuePair = Simplex.optimizeAll(new Rosen(),
        new Bounds(-10.0, 10.0), new Bounds(-10.0, 10.0));
    Assert.assertEquals(valuePair.getPoint(), new double[] {1.0, 1.0}, 0.1);
  }

  @Test
  public void testInvalid() {
    PointValuePair valuePair = Simplex.CMAES.optimize(new Rosen(),
        new Bounds(-10.0, 0.0, 10.0), new Bounds(-10.0, 0.0, 10.0), new Bounds(-10.0, 0.0));
    Assert.assertEquals(valuePair.getPoint(), new double[] {Double.NaN, Double.NaN, Double.NaN}, 0.1, Arrays.toString(valuePair.getPoint()));
    Assert.assertEquals(valuePair.getValue(), Double.NaN, 0.1);
  }
}