package com.ak.numbers;

import java.util.IntSummaryStatistics;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CoefficientsTest {
  private enum InterpolatorCoefficients implements Coefficients {
    INTERPOLATOR_TEST_AKIMA, INTERPOLATOR_TEST_LINEAR
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
}