package com.ak.rsm;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public class ResistanceOneLayerTest {
  @DataProvider(name = "layer-model", parallel = true)
  public Object[][] singleLayerParameters() {
    return new Object[][] {
        {1.0, 20, 40, 21.221},
        {2.0, 20, 40, 21.221 * 2.0},
        {1.0, 40, 80, 10.610},
        {0.5, 40, 80, 10.610 / 2.0},
    };
  }

  @Test(dataProvider = "layer-model")
  public void testOneLayer(double rho, double smm, double lmm, double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MetricPrefix.MILLI(Units.METRE));
    UnivariateFunction resistance = new ResistanceOneLayer(system);
    Assert.assertEquals(resistance.value(rho), rOhm, 0.001);
  }
}