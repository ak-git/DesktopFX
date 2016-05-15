package com.ak.math;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.StreamSupport;

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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EyeTest {
  private EyeTest() {
  }

  @DataProvider(name = "circle", parallel = true)
  public static Object[][] singleLayerParameters() {
    return new Object[][] {
        {Arrays.asList(new Point(2.0, 0.0), new Point(-1.0, 0.0)), 2.0, 1.0 / 2.0},
        {Arrays.asList(new Point(2.0, 0.0), new Point(-1.0, 0.0)), 5.0, (9.0 + 16.0) / 2.0},
        {Arrays.asList(new Point(2.0, 0.0), new Point(-1.0, 0.0), new Point(1.0, 0.0)), 1.0, 1.0 / 3.0},
    };
  }

  @Test(dataProvider = "circle")
  public void testCircleInequality(Iterable<Point> points, double radiusEstimate, double error) {
    DoubleUnaryOperator inequality = new CircleInequality(StreamSupport.stream(points.spliterator(), true));
    Assert.assertEquals(inequality.applyAsDouble(radiusEstimate), error, Float.MIN_NORMAL);
  }

  @Test
  public static void testRosenbrockNelderMeadSimplex() {
    SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-3);
    PointValuePair optimum = optimizer.optimize(new MaxEval(100), new ObjectiveFunction(new Ellipse()),
        GoalType.MINIMIZE, new NelderMeadSimplex(2, 0.1), new InitialGuess(new double[] {1.0, 1.0})
    );
    System.out.println(Arrays.toString(optimum.getPoint()));
  }

  private static class Ellipse implements MultivariateFunction {
    @Override
    public double value(double[] x) {
      double r = x[0];
      double transform = Math.sqrt(x[1]);
      return new CircleInequality(StreamSupport.stream(PointLoader.INSTANCE.getPoints().spliterator(), true).
          map(point -> new Point(point.x() * transform, point.y() / transform))).applyAsDouble(r);
    }
  }
}
