package com.ak.math;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.ak.eye.CircleByPoints;
import com.ak.eye.Point;
import javafx.util.Builder;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.Math.toRadians;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;

public class EyeTest {
  private EyeTest() {
  }

  @DataProvider(name = "circle")
  public static Object[][] circle() {
    return new Object[][] {
        {Arrays.asList(new Point(0, 0), new Point(0, 5), new Point(5, 0))},
        {new EllipsePoints(1).radius(5.0).build()},
    };
  }

  @Test(dataProvider = "circle")
  public void testCircle(List<Point> points) {
    new CircleByPoints(points).get();
  }
}

class EllipsePoints implements Builder<List<Point>> {
  private Stream<Point> pointStream;

  EllipsePoints(int angleStep) {
    pointStream = DoubleStream.iterate(0.0, angle -> angle + angleStep).limit(180 / angleStep + 1).mapToObj(angle ->
        new Point(cos(toRadians(angle)), sin(toRadians(angle))));
  }

  EllipsePoints radius(double radius) {
    pointStream = pointStream.map(point -> point.scale(radius));
    return this;
  }

  @Override
  public List<Point> build() {
    return pointStream.collect(Collectors.toList());
  }
}
