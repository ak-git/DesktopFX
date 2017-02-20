package com.ak.digitalfilter;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleCoefficientsTest {
  private SimpleCoefficientsTest() {
  }

  @Test
  public static void testDiff() {
    Assert.assertEquals(SimpleCoefficients.DIFF.name(), "DIFF");
    Assert.assertEquals(SimpleCoefficients.DIFF.get(), new double[] {-0.5, 0, 0.5});
  }
}