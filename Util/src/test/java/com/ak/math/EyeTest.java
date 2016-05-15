package com.ak.math;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import com.ak.eye.CircleInequality;
import com.ak.eye.Point;
import com.ak.eye.PointLoader;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EyeTest {
  private EyeTest() {
  }

  @Test
  public void testCircleInequality() {
    Iterable<Point> points = Arrays.asList(new Point(2.0, 0.0), new Point(-1.0, 0.0));
    DoubleUnaryOperator inequality = new CircleInequality(points);
    Assert.assertEquals(inequality.applyAsDouble(2.0), 1.0 / 2.0, Float.MIN_NORMAL);
    Assert.assertEquals(inequality.applyAsDouble(1.0), 1.0 / 2.0, Float.MIN_NORMAL);
    Assert.assertEquals(inequality.applyAsDouble(3.0), (1.0 + 4.0) / 2.0, Float.MIN_NORMAL);
    Assert.assertEquals(inequality.applyAsDouble(4.0), (4.0 + 9.0) / 2.0, Float.MIN_NORMAL);
    Assert.assertEquals(inequality.applyAsDouble(5.0), (9.0 + 16.0) / 2.0, Float.MIN_NORMAL);
  }

  @Test
  public static void testRosenbrockNelderMeadSimplex() {
    SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-3);
    PointValuePair optimum = optimizer.optimize(new MaxEval(100), new ObjectiveFunction(new Ellipse()),
        GoalType.MINIMIZE, new NelderMeadSimplex(1, 0.1), new InitialGuess(new double[] {1.0})
    );
    System.out.println(Arrays.toString(optimum.getPoint()));
  }

  private static class Ellipse implements MultivariateFunction {
    @Override
    public double value(double[] x) {
      List<Point> points = PointLoader.INSTANCE.getPoints();
      return new CircleInequality(points).applyAsDouble(x[0]);
    }
  }
}
