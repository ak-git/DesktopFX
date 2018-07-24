package com.ak.rsm;

import java.util.Arrays;

import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class TetrapolarSystemPairTest {
  private TetrapolarSystemPairTest() {
  }

  @DataProvider(name = "tetrapolarPairs")
  public static Object[][] tetrapolarPairs() {
    TetrapolarSystemPair ts = new TetrapolarSystemPair.Builder(0.0, METRE).sPU(1, 2).lCC(3).build();
    return new Object[][] {
        {ts, ts, true},
        {ts, new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(2000.0, 1000.0).lCC(3000.0).build(), true},
        {new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(1.0, 2.0).lCC(3.0).build(),
            new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(1.0, 1.5).lCC(3.0).build(), false}
    };
  }


  @Test(dataProvider = "tetrapolarPairs")
  public static void testEquals(@Nonnull TetrapolarSystemPair system1, TetrapolarSystemPair system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, String.format("%s compared with %s", system1, system2));
    Assert.assertEquals(Arrays.equals(system1.getPair(), system2.getPair()), equals, String.format("%s compared with %s", system1, system2));
  }

  @Test
  public static void testNotEquals() {
    TetrapolarSystemPair pair = new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(10.0, 20.0).lCC(25.0).build();
    Assert.assertNotEquals(pair, new Object());
    Assert.assertNotEquals(pair.equals(new Object()), true);
  }

  @Test(dataProvider = "tetrapolarPairs")
  public static void testHashCode(@Nonnull TetrapolarSystemPair system1, @Nonnull TetrapolarSystemPair system2, boolean equals) {
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, String.format("%s compared with %s", system1, system2));
  }

  @Test(dataProvider = "tetrapolarPairs")
  public static void testToString(@Nonnull TetrapolarSystemPair system1, @Nonnull TetrapolarSystemPair system2, boolean equals) {
    Assert.assertEquals(system1.toString().equals(system2.toString()), equals, String.format("%s compared with %s", system1, system2));
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "sPU.*")
  public static void testInvalidSPU() {
    new TetrapolarSystemPair.Builder(1.0, METRE).build();
  }

  @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "dL == 0")
  public static void testInvalidDL() {
    new TetrapolarSystemPair.Builder(0.0, METRE).buildWithError();
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "lCC.*")
  public static void testInvalidLCC() {
    TetrapolarSystemPair.Builder builder = new TetrapolarSystemPair.Builder(1.0, MILLI(METRE)).sPU(2.0, 2.0).lCC(1.0);
    Assert.assertEquals(builder.getLCC(), 0.001, 0.0001);
    builder.build();
  }

  @Test(dataProvider = "tetrapolarPairs-with-error")
  public static void testWithError(@Nonnull TetrapolarSystemPair[] systems, @Nonnull TetrapolarSystemPair[] systemsE) {
    Assert.assertEquals(systems, systemsE, String.format("%s compared with %s", Arrays.toString(systems), Arrays.toString(systemsE)));
  }

  @DataProvider(name = "tetrapolarPairs-with-error")
  public static Object[][] tetrapolarPairsWithError() {
    return new Object[][] {
        {
            new TetrapolarSystemPair.Builder(Metrics.fromMilli(10.0), METRE).sPU(1.0, 2.0).lCC(3.0).buildWithError(),
            new TetrapolarSystemPair[] {
                new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(1000.0 + 10, 2000.0 - 10).lCC(3000.0 + 10.0).build(),
                new TetrapolarSystemPair.Builder(0.0, MILLI(METRE)).sPU(1000.0 - 10, 2000.0 + 10).lCC(3000.0 - 10.0).build()
            },
        },
    };
  }
}