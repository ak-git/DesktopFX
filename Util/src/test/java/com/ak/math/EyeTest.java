package com.ak.math;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.ak.eye.CircleByPoints;
import com.ak.eye.Point;
import javafx.util.Builder;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.Math.toRadians;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;

public class EyeTest {
  private EyeTest() {
  }

  @DataProvider(name = "circle", parallel = true)
  public static Object[][] circle() {
    return new Object[][] {
        {Arrays.asList(new Point(0, 0), new Point(0, 5), new Point(5, 0)), 2.5, 2.5},
        {Arrays.asList(new Point(0, 5), new Point(5, 0), new Point(0, 0)), 2.5, 2.5},
        {Arrays.asList(new Point(5 + 1, -1), new Point(1, -1), new Point(1, 5 - 1)), 3.5, 1.5},
        {new EllipsePoints(1).radius(5.0).move(11.0, -1.1).noise(0.00001).build(), 11.0, -1.1},
    };
  }

  @Test(dataProvider = "circle")
  public void testCircle(List<Point> points, double cx, double cy) {
    Point point = new CircleByPoints(points).getOrigin();
    Assert.assertEquals(point.x(), cx, 0.1);
    Assert.assertEquals(point.y(), cy, 0.1);
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

  EllipsePoints move(double dx, double dy) {
    pointStream = pointStream.map(point -> point.move(dx, dy));
    return this;
  }

  EllipsePoints noise(double dev) {
    Random random = new Random();
    pointStream = pointStream.map(point -> point.move(random.nextGaussian() * dev, random.nextGaussian() * dev));
    return this;
  }

  @Override
  public List<Point> build() {
    return pointStream.collect(Collectors.toList());
  }
}
