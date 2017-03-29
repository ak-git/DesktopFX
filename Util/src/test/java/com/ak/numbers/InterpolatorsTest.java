package com.ak.numbers;

import org.testng.annotations.Test;

public class InterpolatorsTest {
  private InterpolatorsTest() {
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Number 3 of coefficients DIFF is not even")
  public static void testInvalidCoefficients() {
    Interpolators.interpolator(SimpleCoefficients.DIFF);
  }
}