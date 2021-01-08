package com.ak.rsm;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InexactTetrapolarSystemTest {
  @DataProvider(name = "tetrapolar-systems")
  public static Object[][] tetrapolarSystems() {
    InexactTetrapolarSystem ts = InexactTetrapolarSystem.si(0.1).s(10.0).l(20.0);
    return new Object[][] {
        {ts, ts, true},
        {ts, InexactTetrapolarSystem.si(0.1).s(20.0).l(10.0), true},
        {ts, InexactTetrapolarSystem.si(0.1).s(20.0).l(30.0), false}
    };
  }

  @Test(dataProvider = "tetrapolar-systems")
  public void testEquals(InexactTetrapolarSystem system1, InexactTetrapolarSystem system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, "%s compared with %s".formatted(String.valueOf(system1), system2));
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, "%s compared with %s".formatted(String.valueOf(system1), system2));
    Assert.assertNotEquals(system1, new Object());
    Assert.assertNotEquals(new Object(), system1);
  }

  @Test
  public void testNotEquals() {
    InexactTetrapolarSystem system = InexactTetrapolarSystem.milli(0.01).s(10.0).l(30.0);
    Assert.assertNotEquals(system, new Object());
    Assert.assertNotEquals(new Object(), system);
  }

  @DataProvider(name = "inexact-tetrapolar-systems")
  public static Object[][] inexactTetrapolarSystems() {
    return new Object[][] {
        {InexactTetrapolarSystem.milli(0.1).s(10.0).l(20.0)},
        {InexactTetrapolarSystem.milli(0.1).s(20.0).l(10.0)},
    };
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testToString(@Nonnull InexactTetrapolarSystem system) {
    Assert.assertTrue(system.toString().startsWith(system.getSystem().toString()), system.toString());
    Assert.assertTrue(system.toString().contains("%.1f".formatted(0.1)), system.toString());
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testGetDeltaApparent(@Nonnull InexactTetrapolarSystem system) {
    Assert.assertEquals(system.getDeltaApparent(), 6.0 * 0.1 / 20.0, 1.0e-6, system.toString());
  }

  @Test
  public void testShift() {
    InexactTetrapolarSystem initial = InexactTetrapolarSystem.milli(0.1).s(20.0).l(10.0);
    Assert.assertEquals(initial.getSystem().toRelative().errorFactor(), 6.0, 0.01);
    Assert.assertEquals(initial.shift(1, -1).toRelative().errorFactor(), 5.97, 0.01);
    Assert.assertEquals(initial.shift(-1, -1).toRelative().errorFactor(), 5.99, 0.01);
    Assert.assertEquals(initial.shift(1, 1).toRelative().errorFactor(), 6.01, 0.01);
    Assert.assertEquals(initial.shift(-1, 1).toRelative().errorFactor(), 6.03, 0.01);
  }
}