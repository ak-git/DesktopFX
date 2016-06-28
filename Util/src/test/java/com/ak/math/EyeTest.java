package com.ak.math;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import javafx.util.Builder;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.Math.toRadians;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sin;

public class EyeTest {
  private EyeTest() {
  }

  @DataProvider(name = "circle", parallel = true)
  public static Object[][] circle() {
    return new Object[][] {
        {Arrays.asList(new Vector2D(0, 0), new Vector2D(0, 5), new Vector2D(5, 0)), 2.5, 2.5, 2.5 * 1.4142135623730951},
        {Arrays.asList(new Vector2D(0, 5), new Vector2D(5, 0), new Vector2D(0, 0)), 2.5, 2.5, 2.5 * 1.4142135623730951},
        {Arrays.asList(new Vector2D(5 + 1, -1), new Vector2D(1, -1), new Vector2D(1, 5 - 1)), 3.5, 1.5, 2.5 * 1.4142135623730951},
        {new EllipsePoints(1).radius(5.0).move(11.0, -1.1).noise(0.00001).build(), 11.0, -1.1, 5.0},
    };
  }

  @Test(dataProvider = "circle")
  public void testCircle(List<Vector2D> points, double cx, double cy, double radius) {
  }

  @DataProvider(name = "ellipse")
  public static Object[][] ellipse() {
    return new Object[][] {
        {new EllipsePoints(1).transform(0.5 * 0.5).rotate(30).move(10.0, -1.0).build()},
        {new EllipsePoints(1).transform(0.5 * 0.5).move(10.0, -1.0).rotate(30).build()},
        {new EllipsePoints(1).transform(0.5 * 0.5).move(10, 10).rotate(45).build()},
        {new EllipsePoints(1).build()},
    };
  }

  @Test(dataProvider = "ellipse")
  public void testEllipse(@Nonnull List<Vector2D> points) {
    if (points.size() < 5) {
      throw new IllegalArgumentException(points.toString());
    }
    double[][] coeff = new double[points.size()][5];
    for (int i = 0; i < points.size(); i++) {
      double x = points.get(i).getX();
      double y = points.get(i).getY();
      coeff[i][0] = pow(x, 2.0);
      coeff[i][1] = 2.0 * x * y;
      coeff[i][2] = pow(y, 2.0);
      coeff[i][3] = 2.0 * x;
      coeff[i][4] = 2.0 * y;
    }
    RealMatrix matrix = new Array2DRowRealMatrix(coeff, false);
    DecompositionSolver solver = new SingularValueDecomposition(matrix).getSolver();

    double[] b = new double[points.size()];
    Arrays.fill(b, -1.0);
    RealVector solution = solver.solve(new ArrayRealVector(b, false));
    double a11 = solution.getEntry(0);
    double a12 = solution.getEntry(1);
    double a22 = solution.getEntry(2);
    double a13 = solution.getEntry(3);
    double a23 = solution.getEntry(4);

    double phi = StrictMath.atan(2.0 * a12 / (a11 - a22)) / 2.0;
    double d = a11 * a22 - a12 * a12;
    double x0 = -(a13 * a22 - a12 * a23) / d;
    double y0 = -(a11 * a23 - a13 * a12) / d;
    System.out.printf("%.1f (%.1f; %.1f) %n", Math.toDegrees(phi), x0, y0);
  }
}

class EllipsePoints implements Builder<List<Vector2D>> {
  @Nonnull
  private Stream<Vector2D> pointStream;

  EllipsePoints(int angleStep) {
    pointStream = DoubleStream.iterate(0.0, angle -> angle + angleStep).limit(180 / angleStep + 1).mapToObj(angle ->
        new Vector2D(cos(toRadians(angle)), sin(toRadians(angle))));
  }

  @Nonnull
  EllipsePoints radius(double radius) {
    pointStream = pointStream.map(point -> point.scalarMultiply(radius));
    return this;
  }

  @Nonnull
  EllipsePoints transform(double bToa) {
    pointStream = pointStream.map(point -> {
      double k = Math.sqrt(bToa);
      return new Vector2D(point.getX() / k, point.getY() * k);
    });
    return this;
  }

  @Nonnull
  EllipsePoints rotate(double angleGrad) {
    pointStream = pointStream.map(point -> {
      double x = point.getX();
      double y = point.getY();
      double theta = toRadians(angleGrad);
      return new Vector2D(x * cos(theta) - y * sin(theta), x * sin(theta) + y * cos(theta));
    });
    return this;
  }

  @Nonnull
  EllipsePoints move(double dx, double dy) {
    pointStream = pointStream.map(point -> point.add(new Vector2D(dx, dy)));
    return this;
  }

  @Nonnull
  EllipsePoints noise(double dev) {
    Random random = new Random();
    pointStream = pointStream.map(point -> point.add(new Vector2D(random.nextGaussian() * dev, random.nextGaussian() * dev)));
    return this;
  }

  @Override
  @Nonnull
  public List<Vector2D> build() {
    return pointStream.collect(Collectors.toList());
  }
}
