package com.ak.math;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.eye.PointLoader;
import javafx.util.Builder;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.EigenDecomposition;
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

  @DataProvider(name = "ellipse")
  public static Object[][] ellipse() {
    return new Object[][] {
        {new EllipsePoints(1).transform(0.5 * 0.5).rotate(30).move(10.0, -1.0).build()},
        {new EllipsePoints(1).transform(0.5 * 0.5).move(10.0, -1.0).rotate(30).build()},
        {new EllipsePoints(1).transform(0.5 * 0.5).move(10, 10).rotate(45).build()},
        {new EllipsePoints(1).build()},
        {PointLoader.INSTANCE.getPoints()},
    };
  }

  @Test(dataProvider = "ellipse")
  public void testEllipse(@Nonnull List<Vector2D> points) {
    if (points.size() < 5) {
      return;
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

    EigenDecomposition D = new EigenDecomposition(new Array2DRowRealMatrix(new double[][] {{a11, a12}, {a12, a22}}, false));
    EigenDecomposition X0 = new EigenDecomposition(new Array2DRowRealMatrix(new double[][] {{a13, a12}, {a23, a22}}, false));
    EigenDecomposition Y0 = new EigenDecomposition(new Array2DRowRealMatrix(new double[][] {{a11, a13}, {a12, a23}}, false));
    double x0 = -X0.getDeterminant() / D.getDeterminant();
    double y0 = -Y0.getDeterminant() / D.getDeterminant();


    EigenDecomposition A = new EigenDecomposition(new Array2DRowRealMatrix(new double[][] {{a11, a12, a13}, {a12, a22, a23}, {a13, a23, 1.0}}, false));

    double bAxis = Math.sqrt(-A.getDeterminant() / (D.getDeterminant() * D.getRealEigenvalues()[0]));
    double aAxis = Math.sqrt(-A.getDeterminant() / (D.getDeterminant() * D.getRealEigenvalues()[1]));

    System.out.printf("%.1f (%.1f; %.1f)  %.1f; %.1f %n", Math.toDegrees(phi), x0, y0, bAxis, aAxis);
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
