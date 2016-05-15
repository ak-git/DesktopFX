package com.ak.eye;

import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CircleInequality implements DoubleUnaryOperator {
  private final Stream<Point> points;

  public CircleInequality(Stream<Point> points) {
    this.points = points;
  }

  @Override
  public double applyAsDouble(double r) {
    return points.map(point -> StrictMath.pow(r - StrictMath.hypot(point.x(), point.y()), 2)).
        collect(Collectors.averagingDouble(Double::doubleValue));
  }
}
