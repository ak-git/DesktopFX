package com.ak.numbers.rcm;

import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.numbers.Coefficients;
import com.ak.numbers.Interpolators;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RcmCoefficientsTest {
  private RcmCoefficientsTest() {
  }

  @DataProvider(name = "rcm-coefficients")
  public static Object[][] aperCoefficients() {
    return new Object[][] {
        {RcmCoefficients.CC_ADC_TO_OHM_1, 20},
        {RcmCoefficients.CC_ADC_TO_OHM_2, 20},
        {RcmCoefficients.RHEO_ADC_TO_260_MILLI_1, 16},
        {RcmCoefficients.RHEO_ADC_TO_260_MILLI_2, 16},
        {RcmCoefficients.BR_F005, 12},
        {RcmCoefficients.BR_F025, 31},
        {RcmCoefficients.BR_F200, 27},
    };
  }

  @Test
  public static void testCoefficients() {
    Assert.assertEquals(RcmCoefficients.values().length, 7);

    IntUnaryOperator rheo260ADC = Interpolators.interpolator(RcmCoefficients.RHEO_ADC_TO_260_MILLI_1).get();
    Assert.assertEquals(rheo260ADC.applyAsInt(100), 1054);
    Assert.assertEquals(rheo260ADC.applyAsInt(1300), 911);
  }

  @Test(dataProvider = "rcm-coefficients")
  public static void testCoefficients(@Nonnull Coefficients coefficients, @Nonnegative int count) {
    Assert.assertEquals(coefficients.get().length, count, coefficients.name());
  }
}