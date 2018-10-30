package com.ak.numbers.aper;

import java.util.function.Supplier;

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
        {AperCoefficients.ADC_TO_OHM, 16},
        {AperSurfaceCoefficientsChannel1.CCU_VADC_0, 4},
        {AperSurfaceCoefficientsChannel1.CCU_VADC_15100, 16},
        {AperSurfaceCoefficientsChannel1.CCU_VADC_30200, 14},
        {AperSurfaceCoefficientsChannel1.CCU_VADC_90400, 12},
        {AperSurfaceCoefficientsChannel1.CCU_VADC_301400, 10},

        {AperSurfaceCoefficientsChannel2.CCU_VADC_0, 4},
        {AperSurfaceCoefficientsChannel2.CCU_VADC_15100, 16},
        {AperSurfaceCoefficientsChannel2.CCU_VADC_30200, 14},
        {AperSurfaceCoefficientsChannel2.CCU_VADC_90400, 12},
        {AperSurfaceCoefficientsChannel2.CCU_VADC_301400, 10},
    };
  }

  @Test
  public static void testCoefficients() {
    Assert.assertEquals(AperCoefficients.values().length, 1);
    Assert.assertEquals(AperSurfaceCoefficientsChannel1.values().length, 5);
    Assert.assertEquals(AperSurfaceCoefficientsChannel2.values().length, 5);
  }

  @Test(dataProvider = "aper-coefficients")
  public static void testCoefficients(@Nonnull Supplier<double[]> coefficients, @Nonnegative int count) {
    Assert.assertEquals(coefficients.get().length, count, coefficients.toString());
  }
}