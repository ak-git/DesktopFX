package com.ak.rsm;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.measure.Quantity;
import javax.measure.quantity.Length;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.quantity.Quantities;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class TetrapolarSystemPairTest {
  private TetrapolarSystemPairTest() {
  }

  @DataProvider(name = "tetrapolarPairs", parallel = true)
  public static Object[][] tetrapolarPairs() {
    TetrapolarSystemPair ts = new TetrapolarSystemPair(1.0, 2.0, 3.0, METRE);
    return new Object[][] {
        {ts, ts, true},
        {ts, new TetrapolarSystemPair(2000.0, 1000.0, 3000.0, MILLI(METRE)), true},
        {new TetrapolarSystemPair(1.0, 2.0, 3.0, MILLI(METRE)),
            new TetrapolarSystemPair(1.0, 1.5, 3.0, MILLI(METRE)), false}
    };
  }


  @Test(dataProvider = "tetrapolarPairs")
  public static void testEquals(@Nonnull TetrapolarSystemPair system1, TetrapolarSystemPair system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, String.format("%s compared with %s", system1, system2));
    Assert.assertEquals(Arrays.equals(system1.getPair(), system2.getPair()), equals, String.format("%s compared with %s", system1, system2));
  }

  @Test
  public static void testNotEquals() {
    Assert.assertFalse(new TetrapolarSystemPair(10.0, 20.0, 25.0, MILLI(METRE)).equals(new Object()));
  }

  @Test(dataProvider = "tetrapolarPairs")
  public static void testHashCode(@Nonnull TetrapolarSystemPair system1, @Nonnull TetrapolarSystemPair system2, boolean equals) {
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, String.format("%s compared with %s", system1, system2));
  }

  @Test(dataProvider = "tetrapolarPairs")
  public static void testToString(@Nonnull TetrapolarSystemPair system1, @Nonnull TetrapolarSystemPair system2, boolean equals) {
    Assert.assertEquals(system1.toString().equals(system2.toString()), equals, String.format("%s compared with %s", system1, system2));
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testNotClone() throws CloneNotSupportedException {
    new TetrapolarSystemPair(1.0, 2.0, 3.0, METRE).clone();
  }

  @DataProvider(name = "tetrapolarPairs-with-error", parallel = true)
  public static Object[][] tetrapolarPairsWithError() {
    return new Object[][] {
        {
            new TetrapolarSystemPair(1.0, 2.0, 3.0, METRE),
            new TetrapolarSystemPair(1000.0 + 10, 2000.0 + 10, 3000.0 - 10.0, MILLI(METRE)),
            Quantities.getQuantity(10.0, MILLI(METRE))
        },
        {
            new TetrapolarSystemPair(1.0, 2.0, 3.0, METRE),
            new TetrapolarSystemPair(1000.0 - 20, 2000.0 - 20, 3000.0 + 20.0, MILLI(METRE)),
            Quantities.getQuantity(-20.0, MILLI(METRE))
        },

    };
  }

  @Test(dataProvider = "tetrapolarPairs-with-error")
  public static void testNewWithError(@Nonnull TetrapolarSystemPair system, @Nonnull TetrapolarSystemPair systemE, @Nonnull Quantity<Length> err) {
    Assert.assertTrue(system.newWithError(err.getValue().doubleValue(), err.getUnit()).equals(systemE),
        String.format("%s compared with %s", system, systemE));

  }
}