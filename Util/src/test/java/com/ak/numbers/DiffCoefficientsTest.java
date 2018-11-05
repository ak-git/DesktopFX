package com.ak.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DiffCoefficientsTest {
  private DiffCoefficientsTest() {
  }

  @Test
  public static void testDiff() {
    Assert.assertEquals(DiffCoefficients.DIFF.name(), "DIFF");
    Assert.assertEquals(DiffCoefficients.DIFF.get(), new double[] {-0.5, 0, 0.5});
  }
}