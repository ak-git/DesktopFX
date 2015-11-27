package com.ak.rsm;

import javax.measure.Quantity;
import javax.measure.quantity.ElectricResistance;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.quantity.Quantities;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.OHM;

public class TetrapolarSystemTest {
  private TetrapolarSystemTest() {
  }

  @DataProvider(name = "tetrapolar-systems", parallel = true)
  public static Object[][] tetrapolarSystems() {
    return new Object[][] {
        {new TetrapolarSystem(1.0, 2.0, METRE), new TetrapolarSystem(1000.0, 2000.0, MILLI(METRE)), true},
        {new TetrapolarSystem(1.0, 2.0, MILLI(METRE)), new TetrapolarSystem(1.0, 3.0, MILLI(METRE)), false}
    };
  }

  @DataProvider(name = "tetrapolar-systems-2", parallel = true)
  public static Object[][] tetrapolarSystems2() {
    return new Object[][] {
        {new TetrapolarSystem(0.030, 0.06, METRE), Quantities.getQuantity(1000, MILLI(OHM)), Math.PI * 9.0 / 400.0},
        {new TetrapolarSystem(30.0, 90.0, MILLI(METRE)), Quantities.getQuantity(1.0 / Math.PI, OHM), 3.0 / 50.0},
        {new TetrapolarSystem(40.0, 80.0, MILLI(METRE)), Quantities.getQuantity(1.0 / Math.PI, OHM), 3.0 / 100.0},
    };
  }

  @Test(dataProvider = "tetrapolar-systems")
  public static void testEquals(TetrapolarSystem system1, TetrapolarSystem system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, String.format("%s compared with %s", system1, system2));
  }

  @Test(dataProvider = "tetrapolar-systems-2")
  public static void testApparentResistivity(TetrapolarSystem system, Quantity<ElectricResistance> resistance,
                                             double specificResistance) {
    Assert.assertEquals(system.getApparent(resistance), specificResistance, 1.0e-6);
  }
}