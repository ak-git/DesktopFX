package com.ak.rsm;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class DerivativeRbyHNormalizedByLTest {
  private DerivativeRbyHNormalizedByLTest() {
  }

  @DataProvider(name = "layer-model", parallel = true)
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {(1.0 - 5.0) / (1.0 + 5.0), 0.5, 0.09, 9.005},
        {-0.9, 0.1, 0.1231, 17.505},
        {0.9, 0.1, 0.0086, -28.406},
        {-0.9, 0.9, 0.017425, 92.022},
        {0.9, 0.9, 0.001253, -132.685},
        {-0.6, 0.5, 0.095801, 7.444},
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testOneLayer(double k12, double sToL, double hToL, double rByRho2N) {
    Assert.assertEquals(new DerivativeRbyHNormalizedByL(k12, sToL).value(hToL), rByRho2N, 0.001);
  }
}