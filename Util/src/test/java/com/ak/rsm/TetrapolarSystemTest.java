package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TetrapolarSystemTest {
  @DataProvider(name = "tetrapolar-systems")
  public static Object[][] tetrapolarSystems() {
    TetrapolarSystem ts = TetrapolarSystem.milli().s(2000.0).l(1000.0);
    return new Object[][] {
        {ts, ts, true},
        {ts, TetrapolarSystem.milli().s(1000.0).l(2000.0), true},
        {TetrapolarSystem.milli().s(1.0).l(2.0), TetrapolarSystem.milli().s(1.0).l(3.0), false}
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
    Assert.assertNotEquals(TetrapolarSystem.milli().s(1.0).l(2.0), new Object());
    Assert.assertNotEquals(new Object(), TetrapolarSystem.milli().s(1.0).l(2.0));
  }

  @Test
  public void testL() {
    Assert.assertEquals(TetrapolarSystem.milli().s(2000.0).l(1000.0).getMaxL(), 2.0);
    Assert.assertEquals(TetrapolarSystem.milli().s(2000.0).l(10000.0).getMaxL(), 10.0);
  }

  @DataProvider(name = "tetrapolarSystemsWithErrors")
  public static Object[][] tetrapolarSystemWithErrors() {
    return new Object[][] {
        {TetrapolarSystem.milli().s(1.0).l(2.0).newWithError(Metrics.fromMilli(0.1), 1, -1), 4.0e-4, 0.0015},
        {TetrapolarSystem.milli().s(2.0).l(1.0).newWithError(Metrics.fromMilli(0.1), -1, 1), 4.0e-4, 0.0015},

        {TetrapolarSystem.milli().s(1.0).l(2.0).newWithError(Metrics.fromMilli(0.1), -1, 1), 6.0e-4, 0.0015},
        {TetrapolarSystem.milli().s(2.0).l(1.0).newWithError(Metrics.fromMilli(0.1), 1, -1), 6.0e-4, 0.0015},
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
        {TetrapolarSystem.milli().s(30.0).l(60.0), 1.0, Math.PI * 9.0 / 400.0},
        {TetrapolarSystem.milli().s(90.0).l(30.0), 1.0 / Math.PI, 3.0 / 50.0},
        {TetrapolarSystem.milli().s(40.0).l(80.0), 1.0 / Math.PI, 3.0 / 100.0},
    };
  }

  @Test(dataProvider = "system-apparent")
  public void testApparentResistivity(@Nonnull TetrapolarSystem system, @Nonnegative double resistance,
                                      @Nonnegative double specificResistance) {
    Assert.assertEquals(system.getApparent(resistance), specificResistance, 1.0e-6);
  }
}