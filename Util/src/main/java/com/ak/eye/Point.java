package com.ak.eye;

import java.util.function.UnaryOperator;

public final class Point {
  private final double x;
  private final double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double x() {
    return x;
  }

  public double y() {
    return y;
  }

  public static UnaryOperator<Point> transform(double koeff) {
    double k = Math.sqrt(koeff);
    return point -> new Point(point.x() * k, point.y() / k);
  }

  @Override
  public String toString() {
    return String.format("Point{x=%s, y=%s}", x, y);
  }
}
