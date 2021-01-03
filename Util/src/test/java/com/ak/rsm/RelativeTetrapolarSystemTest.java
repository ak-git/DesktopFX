package com.ak.rsm;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RelativeTetrapolarSystemTest {
  @DataProvider(name = "tetrapolar-systems")
  public static Object[][] tetrapolarSystems() {
    RelativeTetrapolarSystem ts = new RelativeTetrapolarSystem(2.0);
    return new Object[][] {
        {ts, ts, true},
        {ts, new RelativeTetrapolarSystem(1.0 / 2.0), true},
        {new RelativeTetrapolarSystem(1.0 / 2.0), new RelativeTetrapolarSystem(1.0 / 3.0), false}
    };
  }

  @Test(dataProvider = "tetrapolar-systems")
  public void testEquals(RelativeTetrapolarSystem system1, RelativeTetrapolarSystem system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, "%s compared with %s".formatted(String.valueOf(system1), system2));
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, "%s compared with %s".formatted(String.valueOf(system1), system2));
    Assert.assertNotEquals(system1, new Object());
    Assert.assertNotEquals(new Object(), system1);
  }

  @Test
  public void testNotEquals() {
    RelativeTetrapolarSystem system = new RelativeTetrapolarSystem(1.0 / 2.0);
    Assert.assertNotEquals(system, new Object());
    Assert.assertNotEquals(new Object(), system);
  }

  @Test
  public void testToString() {
    RelativeTetrapolarSystem system = new RelativeTetrapolarSystem(1.0 / 3.0);
    Assert.assertEquals(system.toString(), "s / L = %.3f".formatted(1.0 / 3.0), system.toString());
  }

  @Test
  public void testHash() {
    RelativeTetrapolarSystem system = new RelativeTetrapolarSystem(3.0);
    Assert.assertEquals(system.hashCode(), Double.hashCode(1.0 / 3.0), system.toString());
  }
}