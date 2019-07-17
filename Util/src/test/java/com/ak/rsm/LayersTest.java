package com.ak.rsm;

import javax.annotation.Nonnegative;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LayersTest {
  private LayersTest() {
  }

  @DataProvider(name = "k")
  public static Object[][] k() {
    return new Object[][] {
        {1.0, 1.0, 0.0},
        {1.0, Double.POSITIVE_INFINITY, 1.0},
        {1.0, 0.0, -1.0},
        {Double.POSITIVE_INFINITY, 1.0, -1.0},
        {10, 1.0, -9.0 / 11.0},
        {1.0, 10.0, 9.0 / 11.0},
    };
  }

  @Test(dataProvider = "k")
  public static void testGetK12(@Nonnegative double rho1, @Nonnegative double rho2, double k) {
    Assert.assertEquals(Layers.getK12(rho1, rho2), k);
  }

  @DataProvider(name = "rho")
  public static Object[][] rho() {
    return new Object[][] {
        {0.0, 1.0},
        {-1.0, Double.POSITIVE_INFINITY},
        {-9.0 / 11.0, 10.0},
        {9.0 / 11.0, 1.0 / 10.0},
    };
  }

  @Test(dataProvider = "rho")
  public void testGetRho1ToRho2(double k, @Nonnegative double rho1ToRho2) {
    Assert.assertEquals(Layers.getRho1ToRho2(k), rho1ToRho2, 0.001);
  }
}