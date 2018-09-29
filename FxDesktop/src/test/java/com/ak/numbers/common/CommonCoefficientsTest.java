package com.ak.numbers.common;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.numbers.Coefficients;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CommonCoefficientsTest {
  private CommonCoefficientsTest() {
  }

  @DataProvider(name = "coefficients")
  public static Object[][] coefficients() {
    return new Object[][] {
        {CommonCoefficients.RHEO, 61},
        {CommonCoefficients.MYO, 61},
        {CommonCoefficients.ECG, 61},
    };
  }

  @Test
  public static void testCoefficients() {
    Assert.assertEquals(CommonCoefficients.values().length, coefficients().length);
  }

  @Test(dataProvider = "coefficients")
  public static void testCoefficients(@Nonnull Coefficients coefficients, @Nonnegative int count) {
    Assert.assertEquals(coefficients.get().length, count, coefficients.name());
  }
}