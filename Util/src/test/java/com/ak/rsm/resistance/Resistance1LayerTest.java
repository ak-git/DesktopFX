package com.ak.rsm.resistance;

import javax.annotation.Nonnegative;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Resistance1LayerTest {
  @DataProvider(name = "layer-model")
  public static Object[][] singleLayerParameters() {
    return new Object[][] {
        {1.0, 10.0 * 1, 10.0 * 3, 15.915},
        {1.0, 10.0 * 3, 10.0 * 5, 23.873},
    };
  }

  @Test(dataProvider = "layer-model")
  public void testOneLayer(@Nonnegative double rho, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(smm), Metrics.fromMilli(lmm));
    Assert.assertEquals(new Resistance1Layer(system).value(rho), rOhm, 0.001);
    Assert.assertEquals(TetrapolarResistance.milli(smm, lmm).rho(rho).ohms(), rOhm, 0.001);
  }
}