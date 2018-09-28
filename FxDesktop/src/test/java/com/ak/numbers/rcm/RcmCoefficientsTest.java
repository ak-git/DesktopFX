package com.ak.numbers.rcm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.numbers.Coefficients;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RcmCoefficientsTest {
  private RcmCoefficientsTest() {
  }

  @DataProvider(name = "rcm-coefficients")
  public static Object[][] aperCoefficients() {
    return new Object[][] {
        {RcmCoefficients.ADC_TO_OHM_1, 20},
        {RcmCoefficients.ADC_TO_OHM_2, 20},
    };
  }

  @Test
  public static void testCoefficients() {
    Assert.assertEquals(RcmCoefficients.values().length, 2);
  }

  @Test(dataProvider = "rcm-coefficients")
  public static void testCoefficients(@Nonnull Coefficients coefficients, @Nonnegative int count) {
    Assert.assertEquals(coefficients.get().length, count, coefficients.name());
  }
}