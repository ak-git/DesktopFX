package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;

public class TetrapolarSystemTest {
  @DataProvider(name = "tetrapolar-systems")
  public static Object[][] tetrapolarSystems() {
    TetrapolarSystem ts = TetrapolarSystem.si(0.1).s(2.0).l(1.0);
    return new Object[][] {
        {ts, ts, true},
        {ts, TetrapolarSystem.milli(100.0).s(1000.0).l(2000.0), true},
        {TetrapolarSystem.milli(0.1).s(1.0).l(2.0), TetrapolarSystem.milli(0.1).s(1.0).l(3.0), false}
    };
  }

  @Test(dataProvider = "tetrapolar-systems")
  public void testEquals(TetrapolarSystem system1, TetrapolarSystem system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, "%s compared with %s".formatted(system1, system2));
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, "%s compared with %s".formatted(system1, system2));
    Assert.assertNotEquals(system1, new Object());
    Assert.assertNotEquals(new Object(), system1);
  }

  @Test
  public void testNotEquals() {
    Assert.assertNotEquals(TetrapolarSystem.milli(0.1).s(1.0).l(2.0), new Object());
    Assert.assertNotEquals(new Object(), TetrapolarSystem.milli(0.1).s(1.0).l(2.0));
  }

  @Test
  public void testS() {
    Assert.assertEquals(TetrapolarSystem.si(0.1).s(2.0).l(1.0).getS(), 2.0);
    Assert.assertEquals(TetrapolarSystem.milli(0.1).s(2000.0).l(10000.0).getS(), 2.0);
  }

  @Test
  public void testL() {
    Assert.assertEquals(TetrapolarSystem.si(0.1).s(2.0).l(1.0).getL(), 1.0);
    Assert.assertEquals(TetrapolarSystem.milli(0.1).s(2000.0).l(10000.0).getL(), 10.0);
  }

  @Test
  public void testToRelative() {
    Assert.assertEquals(TetrapolarSystem.milli(0.1).s(2000.0).l(1000.0).toRelative(), new RelativeTetrapolarSystem(2.0));
    Assert.assertEquals(TetrapolarSystem.si(0.1).s(2.0).l(10.0).toRelative(), new RelativeTetrapolarSystem(0.2));
  }

  @DataProvider(name = "system-apparent")
  public static Object[][] systemApparent() {
    return new Object[][] {
        {TetrapolarSystem.milli(0.1).s(30.0).l(60.0), 1.0, Math.PI * 9.0 / 400.0},
        {TetrapolarSystem.milli(0.1).s(90.0).l(30.0), 1.0 / Math.PI, 3.0 / 50.0},
        {TetrapolarSystem.milli(0.1).s(40.0).l(80.0), 1.0 / Math.PI, 3.0 / 100.0},
    };
  }

  @Test(dataProvider = "system-apparent")
  public void testApparentResistivity(@Nonnull TetrapolarSystem system, @Nonnegative double resistance,
                                      @Nonnegative double specificResistance) {
    Assert.assertEquals(system.getApparent(resistance), specificResistance, 1.0e-6);
  }

  @Test
  public void testHMax() {
    TetrapolarSystem system = TetrapolarSystem.si(1.0).s(1.0 / 3.0).l(1.0);
    Assert.assertEquals(system.getHMax(1.0), 0.177, 0.001, system.toString());
  }

  @Test
  public void testHMin() {
    TetrapolarSystem system = TetrapolarSystem.milli(0.1).s(10.0).l(30.0);
    Assert.assertEquals(system.getHMin(1.0 / 3.0) / Metrics.fromMilli(30.0),
        0.02, 0.001, system.toString());
  }

  @Test
  public void testToString() {
    TetrapolarSystem ts = TetrapolarSystem.milli(0.0).s(20.0).l(15.0);
    Assert.assertEquals(ts.toString(), "%2.3f x %2.3f %s".formatted(20.0, 15.0, MetricPrefix.MILLI(METRE)));
  }
}