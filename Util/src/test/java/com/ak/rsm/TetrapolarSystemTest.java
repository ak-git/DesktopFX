package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class TetrapolarSystemTest {
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
  public void testEquals(TetrapolarSystem system1, TetrapolarSystem system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, "%s compared with %s".formatted(String.valueOf(system1), system2));
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, "%s compared with %s".formatted(String.valueOf(system1), system2));
    Assert.assertNotEquals(system1, new Object());
    Assert.assertNotEquals(new Object(), system1);
  }

  @Test
  public void testNotEquals() {
    Assert.assertNotEquals(new TetrapolarSystem(1.0, 2.0, METRE), new Object());
    Assert.assertNotEquals(new Object(), new TetrapolarSystem(1.0, 2.0, METRE));
  }

  @Test
  public void testL() {
    Assert.assertEquals(new TetrapolarSystem(2.0, 1.0, METRE).getL(), 2.0);
    Assert.assertEquals(new TetrapolarSystem(2.0, 10.0, METRE).getL(), 10.0);
  }

  @DataProvider(name = "tetrapolarSystemsWithErrors")
  public static Object[][] tetrapolarSystemWithErrors() {
    return new Object[][] {
        {new TetrapolarSystem(1.0, 2.0, MILLI(METRE)).newWithError(Metrics.fromMilli(0.1), 1, -1), 4.0e-4, 0.0015},
        {new TetrapolarSystem(2.0, 1.0, MILLI(METRE)).newWithError(Metrics.fromMilli(0.1), -1, 1), 6.0e-4, 0.0015},

        {new TetrapolarSystem(1.0, 2.0, MILLI(METRE)).newWithError(Metrics.fromMilli(0.1), -1, 1), 6.0e-4, 0.0015},
        {new TetrapolarSystem(2.0, 1.0, MILLI(METRE)).newWithError(Metrics.fromMilli(0.1), 1, -1), 4.0e-4, 0.0015},
    };
  }

  @Test(dataProvider = "tetrapolarSystemsWithErrors")
  public void testRelativeError(@Nonnull TetrapolarSystem system, double radiusMns, double radiusPls) {
    Assert.assertEquals(system.radius(-1.0), radiusMns, 1.0e-5);
    Assert.assertEquals(system.radius(1.0), radiusPls, 1.0e-5);
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
  public void testApparentResistivity(@Nonnull TetrapolarSystem system, @Nonnegative double resistance,
                                      @Nonnegative double specificResistance) {
    Assert.assertEquals(system.getApparent(resistance), specificResistance, 1.0e-6);
  }
}