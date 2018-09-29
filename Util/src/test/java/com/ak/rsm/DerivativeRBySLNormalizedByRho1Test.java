package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.rsm.DerivativeRBySLNormalizedByRho1.DerivateBy.L;
import static com.ak.rsm.DerivativeRBySLNormalizedByRho1.DerivateBy.S;

public class DerivativeRBySLNormalizedByRho1Test {
  private DerivativeRBySLNormalizedByRho1Test() {
  }

  @DataProvider(name = "layer-model")
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {1.0, 0.1, 0.3, 0.2, L, -0.69},
        {1.0, 5.0, 0.5, 0.1, L, -3.687},
        {1.0, 0.1, 1.0 / 3.0, 0.1, L, -0.289},

        {1.0, 5.0, 0.5, 0.5, S, 2.979},
        {1.0, 5.0, 0.5, 0.1, S, 5.571},
        {1.0, 0.1, 1.0 / 3.0, 0.1, S, 0.368},
    };
  }

  @Test(dataProvider = "layer-model")
  public static void test(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double sToL, @Nonnegative double hToL,
                          @Nonnull DerivativeRBySLNormalizedByRho1.DerivateBy derivateBy, double expected) {
    Assert.assertEquals(new DerivativeRBySLNormalizedByRho1(ResistanceTwoLayer.getK12(rho1, rho2), sToL, 1.0, derivateBy).value(hToL), expected, 0.001);
  }
}