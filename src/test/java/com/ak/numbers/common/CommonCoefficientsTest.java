package com.ak.numbers.common;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CommonCoefficientsTest {
  @DataProvider(name = "coefficients")
  public static Object[][] coefficients() {
    return new Object[][] {
        {CommonCoefficients.MYO, 61},
        {CommonCoefficients.ECG, 61},
    };
  }

  @Test
  public void testCoefficients() {
    Assert.assertEquals(CommonCoefficients.values().length, coefficients().length);
  }

  @Test(dataProvider = "coefficients")
  public void testCoefficients(@Nonnull SimpleCoefficients coefficients, @Nonnegative int count) {
    Assert.assertEquals(coefficients.get().length, count, coefficients.name());
  }
}