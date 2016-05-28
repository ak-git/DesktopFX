package com.ak.eye;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public final class CircleByPoints implements Supplier<Point> {
  @Nonnull
  private final List<Point> points;

  public CircleByPoints(@Nonnull List<Point> points) {
    if (points.size() < 3) {
      throw new IllegalArgumentException(points.toString());
    }
    this.points = Collections.unmodifiableList(points);
  }

  @Nonnull
  @Override
  public Point get() {
    Point p1 = points.get(0);
    Point p2 = points.get(1);
    for (Point point : points) {
      Point pStart = point.distance(p1) > point.distance(p2) ? p1 : p2;
      if (p1.distance(p2) < pStart.distance(point)) {
        p1 = pStart;
        p2 = point;
      }
    }

    Point p3 = points.get(2);
    for (Point point : points) {
      if (point.distance(p1) + point.distance(p2) > p3.distance(p1) + p3.distance(p2)) {
        p3 = point;
      }
    }

    return center(new Point[] {p1, p2, p3});
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
