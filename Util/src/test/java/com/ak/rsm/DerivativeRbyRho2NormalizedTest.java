package com.ak.rsm;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DerivativeRbyRho2NormalizedTest {
  private DerivativeRbyRho2NormalizedTest() {
  }

  @DataProvider(name = "layer-model", parallel = true)
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {(1.0 - 5.0) / (1.0 + 5.0), 0.5, 0.126, 0.5},
        {-0.9, 0.1, 0.129568, 0.5},
        {0.9, 0.1, 0.022029, 0.5},
        {-0.9, 0.9, 0.019471, 0.5},
        {0.9, 0.9, 0.006702, 0.5},
        {-0.6, 0.5, 0.132735, 0.5},
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testOneLayer(double k12, double sToL, double hToL, double rByRho2N) {
    Assert.assertEquals(new DerivativeRbyRho2Normalized(k12, sToL).value(hToL), rByRho2N, 0.001);
  }
}