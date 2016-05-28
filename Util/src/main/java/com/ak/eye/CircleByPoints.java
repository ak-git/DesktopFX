package com.ak.eye;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

public final class CircleByPoints {
  @Nonnull
  private final Point origin;
  private final double radius;

  public CircleByPoints(@Nonnull List<Point> points) {
    if (points.size() < 3) {
      throw new IllegalArgumentException(points.toString());
    }
    origin = origin(points);
    radius = radius(points);
  }

  @Nonnull
  public Point getOrigin() {
    return origin;
  }

  public double getRadius() {
    return radius;
  }

  @Nonnull
  private static Point origin(@Nonnull List<Point> points) {
    Point[] p = points.subList(0, 3).toArray(new Point[3]);
    double x = 0.0;
    double y = 0.0;
    int n = 0;
    for (int i = 0; i < points.size() - 2; i++) {
      p[0] = points.get(i);
      for (int j = i + 1; j < points.size() - 1; j++) {
        p[1] = points.get(j);
        for (int k = j + 1; k < points.size(); k++) {
          p[2] = points.get(k);
          Point c = center(p);
          x += c.x();
          y += c.y();
          n++;
        }
      }
    }
    return new Point(x / n, y / n);
  }

  private double radius(@Nonnull Iterable<Point> points) {
    SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-3);
    PointValuePair optimum = optimizer.optimize(new MaxEval(100), new ObjectiveFunction(x -> {
          double r = x[0];
          return StreamSupport.stream(points.spliterator(), true).
              map(point -> StrictMath.pow(point.distance(origin) - r, 2)).
              collect(Collectors.averagingDouble(Double::doubleValue));
        }),
        GoalType.MINIMIZE, new NelderMeadSimplex(1, 1.0e-3), new InitialGuess(new double[] {1.0})
    );
    return optimum.getPoint()[0];
  }

  @Nonnull
  private static Point center(@Nonnull Point[] points) {
    if (points.length != 3) {
      throw new IllegalArgumentException(Arrays.toString(points));
    }

    Point[] p = new Point[points.length];
    double maxK = Double.POSITIVE_INFINITY;
    for (int i = 0; i < p.length; i++) {
      Point[] shifted = new Point[points.length];
      for (int j = 0; j < shifted.length; j++) {
        shifted[j] = points[(i + j) % points.length];
      }
      double k = Math.max(Math.abs(k(shifted[0], shifted[1])), Math.abs(k(shifted[1], shifted[2])));
      if (k < maxK) {
        p = Arrays.copyOf(shifted, p.length);
        maxK = k;
      }
    }

    double ma = k(p[0], p[1]);
    double mb = k(p[1], p[2]);

    double x = ma * mb * (p[0].y() - p[2].y());
    x += mb * (p[0].x() + p[1].x());
    x -= ma * (p[1].x() + p[2].x());
    x /= 2.0 * (mb - ma);

    if (Math.abs(ma) > Math.abs(mb)) {
      return new Point(x, y(x, p[0], p[1]));
    }
    else {
      return new Point(x, y(x, p[1], p[2]));
    }
  }

  private static double k(@Nonnull Point start, @Nonnull Point end) {
    return (end.y() - start.y()) / (end.x() - start.x());
  }

  private static double y(double x, @Nonnull Point start, @Nonnull Point end) {
    return -(1.0 / k(start, end)) * (x - (start.x() + end.x()) / 2.0) + (start.y() + end.y()) / 2.0;
  }
}
