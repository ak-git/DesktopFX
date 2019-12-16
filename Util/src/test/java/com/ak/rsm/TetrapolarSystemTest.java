package com.ak.rsm;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class TetrapolarSystemTest {
  private TetrapolarSystemTest() {
  }

  @DataProvider(name = "tetrapolar-systems")
  public static Object[][] tetrapolarSystems() {
    TetrapolarSystem ts = new TetrapolarSystem(2.0, 1.0, METRE);
    return new Object[][] {
        {ts, ts, true},
        {ts, new TetrapolarSystem(1000.0, 2000.0, MILLI(METRE)), true},
        {new TetrapolarSystem(1.0, 2.0, MILLI(METRE)), new TetrapolarSystem(1.0, 3.0, MILLI(METRE)), false}
    };
  }

  @Test(dataProvider = "tetrapolar-systems")
  public static void testEquals(TetrapolarSystem system1, TetrapolarSystem system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, String.format("%s compared with %s", system1, system2));
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, String.format("%s compared with %s", system1, system2));
    Assert.assertNotEquals(system1, new Object());
    Assert.assertNotEquals(new Object(), system1);
  }

  @Test
  public static void testNotEquals() {
    Assert.assertNotEquals(new TetrapolarSystem(1.0, 2.0, METRE), new Object());
    Assert.assertNotEquals(new Object(), new TetrapolarSystem(1.0, 2.0, METRE));
  }

  @Test
  public static void testLh() {
    Assert.assertEquals(new TetrapolarSystem(1.0, 2.0, METRE).Lh(1), 2.0);
    Assert.assertEquals(new TetrapolarSystem(2.0, 1.0, MILLI(METRE)).Lh(Metrics.fromMilli(2.0)), 0.5);
  }
}