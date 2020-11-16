package com.ak.numbers;

import java.util.IntSummaryStatistics;
import java.util.function.Supplier;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CoefficientsTest {
  @DataProvider(name = "coefficients")
  public static Object[][] coefficients() {
    return new Object[][] {
        {RangeUtils.rangeX(InterpolatorCoefficients.class), 1, 16},
        {RangeUtils.rangeY(InterpolatorCoefficients.class), -100, 100},
    };
  }

  @Test(dataProvider = "coefficients")
  public void testCoefficients(@Nonnull IntSummaryStatistics statistics, int min, int max) {
    Assert.assertEquals(statistics.getMin(), min, statistics.toString());
    Assert.assertEquals(statistics.getMax(), max, statistics.toString());
  }

  @Test
  public void testReverseOrder() {
    Assert.assertEquals(RangeUtils.reverseOrder(new double[] {1.0, 2.0, -10.0, 3.0}), new double[] {3.0, -10.0, 2.0, 1.0});
  }

  @DataProvider(name = "count-coefficients")
  public static Object[][] countCoefficients() {
    return new Object[][] {
        {InterpolatorCoefficients.INTERPOLATOR_TEST_AKIMA, 10},
        {InterpolatorCoefficients.INTERPOLATOR_TEST_LINEAR, 8},
        {InterpolatorCoefficients.INTERPOLATOR_TEST_INVALID, 2},
    };
  }

  @Test(dataProvider = "count-coefficients")
  public void testCoefficients(@Nonnull Supplier<double[]> coefficients, @Nonnegative int count) {
    Assert.assertEquals(coefficients.get().length, count, coefficients.toString());
  }
}