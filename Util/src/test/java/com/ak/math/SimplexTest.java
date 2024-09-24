package com.ak.math;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex.Bounds;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplexTest {
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
  void testOptimizeWithInitialGuessAndBounds() {
    PointValuePair valuePair = Simplex.optimizeAll(new Rosen(),
        new Bounds(10.0, 20.0, 30.0), new Bounds(10.0, 20.0, 30.0)
    );
    assertTrue(
        Arrays.stream(valuePair.getPoint()).anyMatch(value -> Inequality.absolute().applyAsDouble(value, 10.0) < 0.1),
        Arrays.toString(valuePair.getPoint())
    );
  }

  @Test
  void testOptimizeWithBounds() {
    PointValuePair valuePair = Simplex.optimizeAll(new Rosen(),
        new Bounds(-10.0, 10.0), new Bounds(-10.0, 10.0)
    );
    assertThat(valuePair.getPoint()).containsExactly(new double[] {1.0, 1.0}, byLessThan(0.1));
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.NEGATIVE_INFINITY, Double.NaN, Double.POSITIVE_INFINITY})
  void testInfinite(double d) {
    PointValuePair valuePair = Simplex.optimizeAll(ignoreX -> d, new Bounds(-10.0, 10.0));
    assertThat(valuePair.getValue()).isNotFinite();
  }

  @Test
  void testInvalid() {
    PointValuePair valuePair = Simplex.CMAES.optimize(new Rosen(),
        new Bounds(-10.0, 0.0, 10.0), new Bounds(-10.0, 0.0, 10.0), new Bounds(-10.0, 0.0)
    );
    assertThat(valuePair.getPoint()).as(Arrays.toString(valuePair.getPoint())).containsOnly(Double.NaN);
    assertThat(valuePair.getValue()).isNaN();
  }

  @Test
  void testBounds() {
    Bounds bounds = new Bounds(Math.random() - 1.1, Math.random() + 1.1);
    assertThat(bounds.isIn(Math.random())).isTrue();
  }
}