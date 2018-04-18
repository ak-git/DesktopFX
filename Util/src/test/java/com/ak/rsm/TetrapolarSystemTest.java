package com.ak.rsm;

import java.io.IOException;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnull;
import javax.measure.Quantity;
import javax.measure.quantity.ElectricResistance;
import javax.measure.quantity.Length;

import com.ak.util.LineFileBuilder;
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

  @DataProvider(name = "system-apparent", parallel = true)
  public static Object[][] systemApparent() {
    return new Object[][] {
        {new TetrapolarSystem(0.030, 0.06, METRE), 1.0, Math.PI * 9.0 / 400.0},
        {new TetrapolarSystem(30.0, 90.0, MILLI(METRE)), 1.0 / Math.PI, 3.0 / 50.0},
        {new TetrapolarSystem(40.0, 80.0, MILLI(METRE)), 1.0 / Math.PI, 3.0 / 100.0},
    };
  }

  @Test(dataProvider = "system-apparent")
  public static void testApparentResistivity(TetrapolarSystem system, double resistance,
                                             double specificResistance) {
    Assert.assertEquals(system.getApparent(resistance), specificResistance, 1.0e-6);
  }

  @DataProvider(name = "asymmetric-apparent")
  public static Object[][] systemApparent2() {
    return new Object[][] {
        {7.0, 35.0, 7.0, Quantities.getQuantity(103.6, OHM), 7.811},
        {20.0, 80.0, 10.0, Quantities.getQuantity(1.0 / Math.PI / 2.0, OHM), 3.0 / 100.0},
        {40.0, 80.0, 0.0, Quantities.getQuantity(1.0 / Math.PI, OHM), 3.0 / 100.0}
    };
  }

  @Test(dataProvider = "asymmetric-apparent")
  public static void testApparentResistivity2(double smm, double lmm, double centerShift,
                                              @Nonnull Quantity<ElectricResistance> resistance, double specificResistance) {
    TetrapolarSystem sP = new TetrapolarSystem(smm + centerShift * 2, lmm, MILLI(METRE));
    TetrapolarSystem sM = new TetrapolarSystem(smm - centerShift * 2, lmm, MILLI(METRE));
    double rho = Math.PI * resistance.to(OHM).getValue().doubleValue() /
        (1.0 / sP.radiusMinus() - 1.0 / sP.radiusPlus() + 1.0 / sM.radiusMinus() - 1.0 / sM.radiusPlus());
    Assert.assertEquals(rho, specificResistance, 1.0e-3);
  }

  @DataProvider(name = "tetrapolar-systems", parallel = true)
  public static Object[][] tetrapolarSystems() {
    TetrapolarSystem ts = new TetrapolarSystem(1.0, 2.0, METRE);
    return new Object[][] {
        {ts, ts, true},
        {ts, new TetrapolarSystem(1000.0, 2000.0, MILLI(METRE)), true},
        {new TetrapolarSystem(1.0, 2.0, MILLI(METRE)), new TetrapolarSystem(1.0, 3.0, MILLI(METRE)), false}
    };
  }

  @Test(dataProvider = "tetrapolar-systems")
  public static void testEquals(TetrapolarSystem system1, TetrapolarSystem system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, String.format("%s compared with %s", system1, system2));
    Assert.assertEquals(system1.getL(), system2.getL(), 0.01, String.format("%s compared with %s", system1, system2));
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, String.format("%s compared with %s", system1, system2));
  }

  @DataProvider(name = "tetrapolar-systems-with-error", parallel = true)
  public static Object[][] tetrapolarSystemsWithError() {
    return new Object[][] {
        {new TetrapolarSystem(2.0, 3.0, METRE), new TetrapolarSystem(2000.0 + 10, 3000.0 - 10.0, MILLI(METRE)),
            Quantities.getQuantity(10.0, MILLI(METRE))},
        {new TetrapolarSystem(2.0, 3.0, METRE), new TetrapolarSystem(2.0 - 0.010, 3.0 + 0.010, METRE),
            Quantities.getQuantity(-10.0, MILLI(METRE))},
    };
  }

  @Test(dataProvider = "tetrapolar-systems-with-error")
  public static void testEqualsWithError(@Nonnull TetrapolarSystem system, @Nonnull TetrapolarSystem systemE, @Nonnull Quantity<Length> err) {
    Assert.assertEquals(system.newWithError(err.getValue().doubleValue(), err.getUnit()), systemE, String.format("%s compared with %s", system, systemE));
  }

  @Test
  public static void testNotEquals() {
    Assert.assertNotEquals(new TetrapolarSystem(1.0, 2.0, METRE), new Object());
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testNotClone() throws CloneNotSupportedException {
    new TetrapolarSystem(1.0, 2.0, METRE).clone();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testInvalidConstructor() {
    new TetrapolarSystem(2.0, 1.0, METRE);
  }

  @Test(enabled = false)
  public static void testApparent() {
    DoubleStream.of(1.0 / 3.0, 0.5).forEachOrdered(sToL -> {
      try {
        LineFileBuilder.of("%.0f %.0f %.6f").
            xRange(1.0, 100.0, 1.0).
            yRange(1.0, 120.0, 1.0).
            generate(String.format("Apparent_Rho_At_%.2f.txt", sToL),
                (r, lmm) -> new TetrapolarSystem(lmm * sToL, lmm, MILLI(METRE)).getApparent(r)
            );
      }
      catch (IOException e) {
        Assert.fail(e.getMessage(), e);
      }
    });
  }
}