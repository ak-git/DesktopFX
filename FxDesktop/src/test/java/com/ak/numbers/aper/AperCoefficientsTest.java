package com.ak.numbers.aper;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.numbers.Coefficients;
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
        {AperCoefficients.ADC_TO_OHM_1, 36},
        {AperCoefficients.ADC_TO_OHM_2, 36},
        {AperSurfaceCoefficients.RI_VADC_0, 4},
        {AperSurfaceCoefficients.RI_VADC_15000, 64},
    };
  }

  @Test
  public static void testCoefficients() {
    Assert.assertEquals(AperCoefficients.values().length, 5);
    Assert.assertEquals(AperSurfaceCoefficients.values().length, 3);
  }

  @Test(dataProvider = "aper-coefficients")
  public static void testCoefficients(@Nonnull Coefficients coefficients, @Nonnegative int count) {
    Assert.assertEquals(coefficients.get().length, count, coefficients.name());
  }
}