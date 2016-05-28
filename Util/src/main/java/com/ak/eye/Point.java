package com.ak.eye;

import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;

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

  @Nonnull
  public static UnaryOperator<Point> transform(double koeff) {
    double k = Math.sqrt(koeff);
    return point -> new Point(point.x() * k, point.y() / k);
  }

  @Nonnull
  public Point scale(double factor) {
    return new Point(x * factor, y * factor);
  }

  @Nonnull
  public Point move(double toX, double toY) {
    return new Point(x + toX, y + toY);
  }

  public double distance(@Nonnull Point p) {
    return StrictMath.hypot(x - p.x, y - p.y);
  }

  @Nonnull
  Point avg(@Nonnull Point other) {
    return new Point((x + other.x) / 2.0, (y + other.y) / 2.0);
  }

  @Override
  @Nonnull
  public String toString() {
    return String.format("Point{x=%.1f, y=%.1f}", x, y);
  }
}
