package com.ak.eye;

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
  public Point scale(double factor) {
    return new Point(x * factor, y * factor);
  }

  @Nonnull
  public Point move(double toX, double toY) {
    return new Point(x + toX, y + toY);
  }

  @Nonnull
  public Point transform(double bToa) {
    double k = Math.sqrt(bToa);
    return new Point(x / k, y * k);
  }

  double distance(@Nonnull Point p) {
    return StrictMath.hypot(x - p.x, y - p.y);
  }

  @Override
  @Nonnull
  public String toString() {
    return String.format("Point{x=%.1f, y=%.1f}", x, y);
  }
}
