package com.ak.eye;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

public final class CircleByPoints {
  @Nonnull
  private final Point origin;

  public CircleByPoints(@Nonnull List<Point> points) {
    if (points.size() < 3) {
      throw new IllegalArgumentException(points.toString());
    }
    origin = origin(points);
  }

  @Nonnull
  public Point getOrigin() {
    return origin;
  }

  @Nonnull
  private static Point origin(@Nonnull List<Point> points) {
    Point[] p = points.subList(0, 3).toArray(new Point[3]);
    Point origin = center(p);
    for (int i = 0; i < points.size() - 2; i++) {
      p[0] = points.get(i);
      for (int j = i + 1; j < points.size() - 1; j++) {
        p[1] = points.get(j);
        for (int k = j + 2; k < points.size(); k++) {
          p[2] = points.get(k);
          origin.avg(center(p));
        }
      }
    }
    return origin;
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
