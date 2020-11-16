package com.ak.inverse;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InequalityTest {
  @Test
  public void testProportional() {
    Inequality inequality = Inequality.proportional();
    Assert.assertEquals(inequality.applyAsDouble(-12, -3), 3.0, 0.01);
    Assert.assertEquals(inequality.getAsDouble(), 3.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(12, -4), 5.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(new double[] {8.0 + 1.0, 8.0 + 1.0, 4.0 + 1.0}, new double[] {1.0, 1.0, 1.0}), 13.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(0.0, 0.0), Double.NaN, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(1.0, 0.0), Double.POSITIVE_INFINITY, 0.01);
  }

  @Test
  public void testL2Absolute() {
    Inequality inequality = Inequality.absolute();
    Assert.assertEquals(inequality.applyAsDouble(0.0, -3.0), 3.0, 0.01);
    Assert.assertEquals(inequality.getAsDouble(), 3.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(4.0, 0.0), 5.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(new double[] {-1.0, 2.0}, new double[] {-1.0, 2.0}), 5.0, 0.01);
    Assert.assertEquals(inequality.applyAsDouble(new double[] {8.0, 0.0, 0.0}, new double[] {0.0, 8.0, 4.0}), 13.0, 0.01);
  }
}