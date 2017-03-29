package com.ak.numbers.aper;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AperCoefficientsTest {
  private AperCoefficientsTest() {
  }

  @DataProvider(name = "aper-coefficients")
  public static Object[][] aperCoefficients() {
    return new Object[][] {
        {AperCoefficients.RHEO, 61},
        {AperCoefficients.MYO, 61},
        {AperCoefficients.ECG, 61},
        {AperCoefficients.I_ADC_TO_OHM, 60},
        {AperCoefficients.R_ADC_15_OHM, 33},
        {AperCoefficients.R_VALUE_15_OHM, 33},
    };
  }

  @Test
  public static void testCoefficients() {
    Assert.assertEquals(AperCoefficients.values().length, aperCoefficients().length);
  }

  @Test(dataProvider = "aper-coefficients")
  public static void testCoefficients(@Nonnull AperCoefficients coefficients, @Nonnegative int count) {
    Assert.assertEquals(coefficients.get().length, count, coefficients.name());
  }
}