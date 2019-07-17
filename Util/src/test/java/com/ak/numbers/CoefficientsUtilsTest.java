package com.ak.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CoefficientsUtilsTest {

  @Test
  public void testSerialize() {
    double[] out = CoefficientsUtils.serialize(new double[] {1.0, -1.0, 3.0, -3.0}, new double[] {1.0, -1.0}, 5);
    Assert.assertEquals(out, new double[] {1.0, 0.0, 3.0, 0.0, 0.0}, 1.0e-3);
  }
}