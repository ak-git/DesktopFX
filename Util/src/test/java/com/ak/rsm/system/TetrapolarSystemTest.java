package com.ak.rsm.system;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;

public class TetrapolarSystemTest {
  @DataProvider(name = "tetrapolar-systems")
  public static Object[][] tetrapolarSystems() {
    TetrapolarSystem ts1 = new TetrapolarSystem(2.0, 1.0);
    TetrapolarSystem ts2 = new TetrapolarSystem(1.0, 2.0);
    TetrapolarSystem ts3 = new TetrapolarSystem(1.0, 3.0);
    return new Object[][] {
        {ts1, ts1, true},
        {ts1, ts2, true},
        {ts1, ts3, false},
        {ts1, new Object(), false},
        {new Object(), ts1, false},
    };
  }

  @Test(dataProvider = "tetrapolar-systems")
  @ParametersAreNonnullByDefault
  public void testEquals(Object system1, Object system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, "%s compared with %s".formatted(system1, system2));
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, "%s compared with %s".formatted(system1, system2));
    Assert.assertNotEquals(system1, null);
  }

  @Test
  public void testL() {
    Assert.assertEquals(new TetrapolarSystem(2.0, 1.0).lCC(), 1.0);
    Assert.assertEquals(new TetrapolarSystem(1.0, 2.0).lCC(), 2.0);
  }

  @Test
  public void testDim() {
    Assert.assertEquals(new TetrapolarSystem(2.0, 1.0).getDim(), 2.0);
    Assert.assertEquals(new TetrapolarSystem(1.0, 2.0).getDim(), 2.0);
  }

  @Test
  public void testToRelative() {
    Assert.assertEquals(new TetrapolarSystem(2.0, 1.0).relativeSystem(), new RelativeTetrapolarSystem(2.0));
    Assert.assertEquals(new TetrapolarSystem(1.0, 2.0).relativeSystem(), new RelativeTetrapolarSystem(0.5));
  }

  @Test
  public void testToString() {
    TetrapolarSystem ts = new TetrapolarSystem(Metrics.fromMilli(20.0), Metrics.fromMilli(15.0));
    Assert.assertEquals(ts.toString(), "%2.3f x %2.3f %s".formatted(20.0, 15.0, MetricPrefix.MILLI(METRE)));
  }
}