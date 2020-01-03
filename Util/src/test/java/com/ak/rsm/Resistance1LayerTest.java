package com.ak.rsm;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class Resistance1LayerTest {
  private Resistance1LayerTest() {
  }

  @DataProvider(name = "layer-model")
  public static Object[][] singleLayerParameters() {
    return new Object[][] {
        {1.0, 20.0, 40.0, 21.221},
        {2.0, 40.0, 20.0, 21.221 * 2.0},
        {1.0, 40.0, 80.0, 10.610},
        {1.0, 80.0, 40.0, 10.610},
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testOneLayer(@Nonnegative double rho, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    Assert.assertEquals(new Resistance1Layer(system).value(rho), rOhm, 0.001);
  }

  @DataProvider(name = "system-apparent")
  public static Object[][] systemApparent() {
    return new Object[][] {
        {new TetrapolarSystem(0.030, 0.06, METRE), 1.0, Math.PI * 9.0 / 400.0},
        {new TetrapolarSystem(90.0, 30.0, MILLI(METRE)), 1.0 / Math.PI, 3.0 / 50.0},
        {new TetrapolarSystem(40.0, 80.0, MILLI(METRE)), 1.0 / Math.PI, 3.0 / 100.0},
    };
  }

  @Test(dataProvider = "system-apparent")
  public static void testApparentResistivity(@Nonnull TetrapolarSystem system, @Nonnegative double resistance,
                                             @Nonnegative double specificResistance) {
    Resistance1Layer r = new Resistance1Layer(system);
    Assert.assertEquals(r.getApparent(resistance), specificResistance, 1.0e-6);
  }

  @Test(dataProviderClass = LayersProvider.class, dataProvider = "theoryStaticParameters")
  public static void testInverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms) {
    Logger.getAnonymousLogger().log(Level.INFO, Resistance1Layer.inverseStatic(systems, rOhms).toString());
  }
}