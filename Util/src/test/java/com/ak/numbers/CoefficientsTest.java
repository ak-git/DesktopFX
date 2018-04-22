package com.ak.numbers;

import java.util.IntSummaryStatistics;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CoefficientsTest {
  private enum InterpolatorCoefficients implements Coefficients {
    INTERPOLATOR_TEST_AKIMA, INTERPOLATOR_TEST_LINEAR, INTERPOLATOR_TEST_JSON
  }

  private CoefficientsTest() {
  }

  @DataProvider(name = "coefficients")
  public static Object[][] coefficients() {
    return new Object[][] {
        {CoefficientsUtils.rangeX(InterpolatorCoefficients.class), 1, 16},
        {CoefficientsUtils.rangeY(InterpolatorCoefficients.class), -100, 100},
    };
  }

  @Test(dataProvider = "coefficients")
  public static void testCoefficients(@Nonnull IntSummaryStatistics statistics, int min, int max) {
    Assert.assertEquals(statistics.getMin(), min, statistics.toString());
    Assert.assertEquals(statistics.getMax(), max, statistics.toString());
  }

  @Test
  public static void testReverseOrder() {
    Assert.assertEquals(CoefficientsUtils.reverseOrder(new double[] {1.0, 2.0, -10.0, 3.0}), new double[] {3.0, -10.0, 2.0, 1.0});
  }

  @DataProvider(name = "count-coefficients")
  public static Object[][] countCoefficients() {
    return new Object[][] {
        {InterpolatorCoefficients.INTERPOLATOR_TEST_AKIMA, 10},
        {InterpolatorCoefficients.INTERPOLATOR_TEST_LINEAR, 8},
        {InterpolatorCoefficients.INTERPOLATOR_TEST_JSON, 0},
    };
  }

  @Test(dataProvider = "count-coefficients")
  public static void testCoefficients(@Nonnull Coefficients coefficients, @Nonnegative int count) {
    Assert.assertEquals(coefficients.get().length, count, coefficients.name());
  }
}