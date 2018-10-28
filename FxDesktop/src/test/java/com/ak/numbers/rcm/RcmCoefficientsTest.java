package com.ak.numbers.rcm;

import java.util.Arrays;
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
        {RcmCoefficients.BR_F005, 10},
        {RcmCoefficients.BR_F025, 25},
        {RcmCoefficients.BR_F200, 22},
    };
  }

  @Test
  public static void testCoefficients() {
    Assert.assertEquals(RcmCoefficients.values().length, 7);

    IntUnaryOperator rheo260ADC = Interpolators.interpolator(RcmCoefficients.RHEO_ADC_TO_260_MILLI_1).get();
    Assert.assertEquals(rheo260ADC.applyAsInt(100), 1054);
    Assert.assertEquals(rheo260ADC.applyAsInt(1300), 911);

    Assert.assertFalse(Arrays.equals(RcmCoefficients.CC_ADC_TO_OHM_1.get(), RcmCoefficients.CC_ADC_TO_OHM_2.get()));
    Assert.assertFalse(Arrays.equals(RcmCoefficients.RHEO_ADC_TO_260_MILLI_1.get(), RcmCoefficients.RHEO_ADC_TO_260_MILLI_2.get()));
  }

  @Test(dataProvider = "rcm-coefficients")
  public static void testCoefficients(@Nonnull Coefficients coefficients, @Nonnegative int count) {
    Assert.assertEquals(coefficients.get().length, count, coefficients.name());
  }
}