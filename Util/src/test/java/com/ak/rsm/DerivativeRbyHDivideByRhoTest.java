package com.ak.rsm;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DerivativeRbyHDivideByRhoTest {
  private DerivativeRbyHDivideByRhoTest() {
  }

  @DataProvider(name = "layer-model", parallel = true)
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {(1.0 - 5.0) / (1.0 + 5.0), 0.5, 0.99, 0.09, 2.208},
        {-0.9, 0.1, 1.0, 0.1231, 0.256},
        {0.9, 0.1, 1.0, 0.0086, -57.883},
        {-0.9, 0.9, 1.0, 0.017425, 54.309},
        {-1.0, 0.5, 1.0, 0.1, 2.622},
        {-1.0, 1.0 / 3.0, 1.0, 1.0, 0.03},
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testLayer(double k12, double s, double l, double hToL, double rByH) {
    Assert.assertEquals(new DerivativeRbyHDivideByRho(k12, s, l).value(hToL), rByH, 0.001);
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testNotClone() throws CloneNotSupportedException {
    new DerivativeRbyHNormalized(1.0, 0.5).clone();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public static void testNotNominator() {
    new DerivativeRbyHNormalized(1.0, 0.5).nominator(1.0);
  }
}