package com.ak.rsm;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public final class ResistanceTwoLayerTest {
  private ResistanceTwoLayerTest() {
  }

  @DataProvider(name = "layer-model", parallel = true)
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {new Double[] {1.0, 1.0}, 0.0, 20, 40, 21.221},
        {new Double[] {2.0, 2.0}, 0.0, 20, 40, 21.221 * 2.0},
        {new Double[] {1.0, 1.0}, 0.0, 40, 80, 10.610},
        {new Double[] {0.5, 0.5}, 0.0, 40, 80, 10.610 / 2.0},

        {new Double[] {3.5, 1.35}, 10, 20, 40, 59.108},
        {new Double[] {5.0, 2.0}, 15, 20, 40, 95.908},
        {new Double[] {7.0, 1.0}, 20, 40, 80, 50.132},
        {new Double[] {9.5, 0.5}, 30, 40, 80, 81.831},

        {new Double[] {20.0, 1.0}, 1.0, 40.0, 80.0, 10.649}
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testLayer(Double[] rho, double hmm, double smm, double lmm, double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MetricPrefix.MILLI(Units.METRE));
    Assert.assertEquals(new ResistanceTwoLayer(system).value(rho[0], rho[1],
        Quantities.getQuantity(hmm, MetricPrefix.MILLI(Units.METRE)).to(Units.METRE).getValue().doubleValue()), rOhm, 0.001);
  }
}