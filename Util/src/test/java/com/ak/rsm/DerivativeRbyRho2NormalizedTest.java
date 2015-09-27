package com.ak.rsm;

import org.apache.commons.math3.analysis.TrivariateFunction;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class DerivativeRbyRho2NormalizedTest {
  @DataProvider(name = "layer-model", parallel = true)
  public Object[][] twoLayerParameters() {
    return new Object[][] {
        {(1.0 - 5.0) / (1.0 + 5.0), 0.5, 0.126, 0.5},
    };
  }

  @Test(dataProvider = "layer-model")
  public void testOneLayer(double k12, double sToL, double hToL, double rByRho2N) {
    TrivariateFunction rbyRho2Normalized = new DerivativeRbyRho2Normalized();
    Assert.assertEquals(rbyRho2Normalized.value(k12, sToL, hToL), rByRho2N, 0.001);
  }
}