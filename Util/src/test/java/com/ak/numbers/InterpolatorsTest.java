package com.ak.numbers;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InterpolatorsTest {
  @Test(expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Number of points 1 from INTERPOLATOR_TEST_INVALID is too small")
  public void testTooLowCoefficients() {
    Interpolators.interpolator(InterpolatorCoefficients.INTERPOLATOR_TEST_INVALID);
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
            0, 0, 0, 8, 15, 23, 30, 38, 46, 53, 61, 69, 76, 84, 91
        }}
    };
  }

  @Test(dataProvider = "interpolators")
  public <C extends Enum<C> & Coefficients> void testInterpolator(@Nonnull C coefficients, @Nonnull int[] expected) {
    IntUnaryOperator operator = Interpolators.interpolator(coefficients).get();
    int[] actual = IntStream.rangeClosed(1, 15).map(operator).toArray();
    Assert.assertTrue(Arrays.equals(actual, expected), Arrays.toString(actual));
  }
}