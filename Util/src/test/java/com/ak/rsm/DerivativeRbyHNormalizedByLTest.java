package com.ak.rsm;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class DerivativeRbyHNormalizedByLTest {
  @DataProvider(name = "layer-model", parallel = true)
  public Object[][] twoLayerParameters() {
    return new Object[][] {
        {(1.0 - 5.0) / (1.0 + 5.0), 0.5, 0.09, 9.005},
        {-0.9, 0.1, 0.1, 14.644},
        {0.9, 0.1, 0.1, -8.143},
        {-0.9, 0.9, 0.1, 4.146},
        {0.9, 0.9, 0.1, -5.585},
        {-0.6, 0.5, 0.1, 7.421},
    };
  }

  @Test(dataProvider = "layer-model")
  public void testOneLayer(double k12, double sToL, double hToL, double rByRho2N) {
    Assert.assertEquals(new DerivativeRbyHNormalizedByL(k12, sToL).value(hToL), rByRho2N, 0.001);
  }
}