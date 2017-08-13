package com.ak.inverse;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InequalityTest {
  private InequalityTest() {
  }

  @Test
  public void testLogDifference() {
    Inequality inequality = Inequality.logDifference();
    Assert.assertEquals(inequality.applyAsDouble(1, StrictMath.pow(Math.E, 3.0)), 3.0, 0.01);
    Assert.assertEquals(inequality.getAsDouble(), 3.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(StrictMath.pow(Math.E, 4.0), 1.0), 5.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(0.0, 0.0), Double.NaN, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(1.0, 0.0), Double.POSITIVE_INFINITY, 0.01);
  }

  @Test
  public void testProportional() {
    Inequality inequality = Inequality.proportional();
    Assert.assertEquals(inequality.applyAsDouble(-12, -3), 3.0, 0.01);
    Assert.assertEquals(inequality.getAsDouble(), 3.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(20, 4), 5.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(0.0, 0.0), Double.NaN, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(1.0, 0.0), Double.POSITIVE_INFINITY, 0.01);
  }

  @Test
  public static void testL2Absolute() {
    Inequality inequality = Inequality.absolute();
    Assert.assertEquals(inequality.applyAsDouble(0, -3), 3.0, 0.01);
    Assert.assertEquals(inequality.getAsDouble(), 3.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(4, 0), 5.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(-1, 11), 13.0, 0.01);
  }
}