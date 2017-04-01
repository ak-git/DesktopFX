package com.ak.numbers;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InterpolatorsTest {
  private enum InterpolatorCoefficients implements Coefficients {
    INTERPOLATOR_TEST_AKIMA, INTERPOLATOR_TEST_LINEAR, INTERPOLATOR_TEST_INVALID
  }

  private InterpolatorsTest() {
  }

  @Test(expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Number 3 of coefficients DIFF is not even")
  public static void testInvalidCoefficients() {
    Interpolators.interpolate(SimpleCoefficients.DIFF);
  }

  @Test(expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Number of points 1 from INTERPOLATOR_TEST_INVALID is too small")
  public static void testTooLowCoefficients() {
    Interpolators.interpolate(InterpolatorCoefficients.INTERPOLATOR_TEST_INVALID);
  }

  @DataProvider(name = "interpolators")
  public static Object[][] interpolators() {
    return new Object[][] {
        {InterpolatorCoefficients.INTERPOLATOR_TEST_AKIMA, new int[] {
            0, 0, 0, 40, 100,
            133, 153, 163, 160, 146,
            120, 83, 33, -27, -100
        }},
        {InterpolatorCoefficients.INTERPOLATOR_TEST_LINEAR, new int[] {
            0, 0, 0, 8, 17, 25, 33, 42, 50, 58, 67, 75, 83, 92, 100
        }}
    };
  }

  @Test(dataProvider = "interpolators")
  public static void testInterpolator(@Nonnull Coefficients coefficients, @Nonnull int[] expected) {
    IntUnaryOperator operator = Interpolators.interpolate(coefficients).get();
    int[] actual = IntStream.rangeClosed(1, 15).map(operator).toArray();
    Assert.assertTrue(Arrays.equals(actual, expected), Arrays.toString(actual));
  }
}