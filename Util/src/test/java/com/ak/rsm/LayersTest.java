package com.ak.rsm;

import java.util.Arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LayersTest {
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
  public void testGetK12(@Nonnegative double rho1, @Nonnegative double rho2, double k) {
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

  @DataProvider(name = "qn")
  public static Object[][] qn() {
    return new Object[][] {{
        0, 0, 1, 4, new double[] {0.0, 0.0, 0.0, 0.0, 0.0}
    }, {
        -1, 0, 1, 0, new double[] {-1.0}
    }, {
        -1, 0.2, 1, 0, new double[] {-1.0}
    }, {
        -1, -1, 1, 2, new double[] {-1.0, 1.0, -1.0}
    }, {
        -1, 1, 1, 1, new double[] {-1.0, 1.0}
    }, {
        -1, 1, 1, 2, new double[] {-1.0, 1.0, -1.0}
    }, {
        -1, 1, 2, 2, new double[] {0.0, -1.0, 0.0, 1.0}
    }, {
        -0.5, 0.5, 1, 4, new double[] {-0.5, 0.25, -0.125, 0.0625, 0.34375}
    }, {
        0.5, -0.5, 1, 1, new double[] {0.5, -0.125}
    }};
  }

  @Test(dataProvider = "qn")
  public void testQ(double k12, double k23, @Nonnegative int p1, @Nonnegative int p2mp1, @Nonnull double[] expected) {
    double[] actual = Arrays.copyOfRange(Layers.qn(k12, k23, p1, p2mp1), 1, p1 + p2mp1 + 1);
    Assert.assertEquals(actual, expected, 1.0e-6, Arrays.toString(actual));
  }
}