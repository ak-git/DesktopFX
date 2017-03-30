package com.ak.numbers;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InterpolatorsTest {
  private enum InterpolatorCoefficients implements Coefficients {
    INTERPOLATOR_TEST
  }

  private InterpolatorsTest() {
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Number 3 of coefficients DIFF is not even")
  public static void testInvalidCoefficients() {
    Interpolators.interpolate(SimpleCoefficients.DIFF);
  }

  @Test
  public static void testInterpolator() {
    IntUnaryOperator operator = Interpolators.interpolate(InterpolatorCoefficients.INTERPOLATOR_TEST).get();
    int[] actual = IntStream.rangeClosed(1, 15).map(operator).toArray();
    Assert.assertTrue(Arrays.equals(actual, new int[] {
        0, 0, 0, 40, 100,
        133, 153, 163, 160, 146,
        120, 83, 33, -27, -100
    }), Arrays.toString(actual));
  }

}