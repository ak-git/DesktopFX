package com.ak.math;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

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
        {Arrays.asList(new Point(0, 0), new Point(0, 5), new Point(5, 0)), 2.5, 2.5, 2.5 * 1.4142135623730951},
        {Arrays.asList(new Point(0, 5), new Point(5, 0), new Point(0, 0)), 2.5, 2.5, 2.5 * 1.4142135623730951},
        {Arrays.asList(new Point(5 + 1, -1), new Point(1, -1), new Point(1, 5 - 1)), 3.5, 1.5, 2.5 * 1.4142135623730951},
        {new EllipsePoints(1).radius(5.0).move(11.0, -1.1).noise(0.00001).build(), 11.0, -1.1, 5.0},
    };
  }

  @Test(dataProvider = "circle")
  public void testCircle(List<Point> points, double cx, double cy, double radius) {
    CircleByPoints circleByPoints = new CircleByPoints(points);
    Assert.assertEquals(circleByPoints.getOrigin().x(), cx, 0.1, "x");
    Assert.assertEquals(circleByPoints.getOrigin().y(), cy, 0.1, "y");
    Assert.assertEquals(circleByPoints.getRadius(), radius, 0.1, "r");
  }

  @DataProvider(name = "ellipse", parallel = true)
  public static Object[][] ellipse() {
    return new Object[][] {
        {new EllipsePoints(45).transform(0.5 * 0.5).build(), 0.0, -3.4},
        {new EllipsePoints(1).transform(0.5 * 0.5).build(), 0.0, -4.0},
    };
  }

  @Test(dataProvider = "ellipse")
  public void testEllipse(List<Point> points, double cx, double cy) {
    CircleByPoints circleByPoints = new CircleByPoints(points);
    Assert.assertEquals(circleByPoints.getOrigin().x(), cx, 0.1, "x");
    Assert.assertEquals(circleByPoints.getOrigin().y(), cy, 0.1, "y");
  }
}

class EllipsePoints implements Builder<List<Point>> {
  @Nonnull
  private Stream<Point> pointStream;

  EllipsePoints(int angleStep) {
    pointStream = DoubleStream.iterate(0.0, angle -> angle + angleStep).limit(180 / angleStep + 1).mapToObj(angle ->
        new Point(cos(toRadians(angle)), sin(toRadians(angle))));
  }

  @Nonnull
  EllipsePoints radius(double radius) {
    pointStream = pointStream.map(point -> point.scale(radius));
    return this;
  }

  @Nonnull
  EllipsePoints transform(double bToa) {
    pointStream = pointStream.map(point -> point.transform(bToa));
    return this;
  }

  @Nonnull
  EllipsePoints move(double dx, double dy) {
    pointStream = pointStream.map(point -> point.move(dx, dy));
    return this;
  }

  @Nonnull
  EllipsePoints noise(double dev) {
    Random random = new Random();
    pointStream = pointStream.map(point -> point.move(random.nextGaussian() * dev, random.nextGaussian() * dev));
    return this;
  }

  @Override
  @Nonnull
  public List<Point> build() {
    return pointStream.collect(Collectors.toList());
  }
}
