package com.ak.inverse;

import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

public class InequalityTest {
  private InequalityTest() {
  }

  @Test
  public void testExpAndLogDifference() {
    Inequality inequality = Inequality.expAndLogDifference();
    Assert.assertEquals(inequality.applyAsDouble(-1.0, 1.0), 2 * (Math.E - 1), 0.01);
    Assert.assertEquals(inequality.applyAsDouble(1.0, -1.0), 2 * (Math.E - 1) * 1.4142135623730951, 0.01);

    inequality = Inequality.expAndLogDifference();
    Assert.assertEquals(inequality.applyAsDouble(0.0, -1.0), Math.E - 1, 0.01);
    inequality = Inequality.expAndLogDifference();
    Assert.assertEquals(inequality.applyAsDouble(-1.0, -2.0), log(3.0) - log(2.0), 0.01);
    Assert.assertEquals(inequality.applyAsDouble(2.0, 1.0), (log(3.0) - log(2.0)) * 1.4142135623730951, 0.01);
  }

  @Test
  public void testLog1pDifference() {
    Inequality inequality = Inequality.log1pDifference();
    Assert.assertEquals(inequality.applyAsDouble(-1.0, -pow(Math.E, 3.0)), 2.35, 0.01);
    Assert.assertEquals(inequality.getAsDouble(), 2.35, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(pow(Math.E, 4.0), 1.0), 4.07, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(new double[] {pow(Math.E, 8.0), pow(Math.E, 8.0), pow(Math.E, 4.0)},
        new double[] {1, 1, 1}), 11.59, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(0.0, 0.0), 11.59, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(-1.0, 0.0), 11.61, 0.01);
  }

  @Test
  public void testLogDifference() {
    Inequality inequality = Inequality.logDifference();
    Assert.assertEquals(inequality.applyAsDouble(1.0, pow(Math.E, 3.0)), 3.0, 0.01);
    Assert.assertEquals(inequality.getAsDouble(), 3.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(pow(Math.E, 4.0), 1.0), 5.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(new double[] {pow(Math.E, 8.0), pow(Math.E, 8.0), pow(Math.E, 4.0)},
        new double[] {1, 1, 1}), 13.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(0.0, 0.0), Double.NaN, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(1.0, 0.0), Double.POSITIVE_INFINITY, 0.01);
  }

  @Test
  public void testProportional() {
    Inequality inequality = Inequality.proportional();
    Assert.assertEquals(inequality.applyAsDouble(-12, -3), 3.0, 0.01);
    Assert.assertEquals(inequality.getAsDouble(), 3.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(20, 4), 5.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(new double[] {8.0 + 1.0, 8.0 + 1.0, 4.0 + 1.0},
        new double[] {1.0, 1.0, 1.0}), 13.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(0.0, 0.0), Double.NaN, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(1.0, 0.0), Double.POSITIVE_INFINITY, 0.01);
  }

  @Test
  public static void testL2Absolute() {
    Inequality inequality = Inequality.absolute();
    Assert.assertEquals(inequality.applyAsDouble(0.0, -3.0), 3.0, 0.01);
    Assert.assertEquals(inequality.getAsDouble(), 3.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(4.0, 0.0), 5.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(new double[] {-1.0, 2.0}, new double[] {-1.0, 2.0}), 5.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(new double[] {8.0, 0.0, 0.0}, new double[] {0.0, 8.0, 4.0}), 13.0, 0.01);
  }
}