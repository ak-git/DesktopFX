package com.ak.eye;

import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class CircleInequality implements DoubleUnaryOperator {
  private final Iterable<Point> points;

  public CircleInequality(Iterable<Point> points) {
    this.points = points;
  }

  @Override
  public double applyAsDouble(double r) {
    return StreamSupport.stream(points.spliterator(), true).
        map(point -> StrictMath.pow(r - StrictMath.hypot(point.x(), point.y()), 2)).
        collect(Collectors.averagingDouble(Double::doubleValue));
  }
}
