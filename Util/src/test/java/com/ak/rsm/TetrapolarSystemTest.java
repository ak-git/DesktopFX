package com.ak.rsm;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.measure.Quantity;
import javax.measure.quantity.ElectricResistance;

import com.ak.util.LineFileCollector;
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

  @DataProvider(name = "tetrapolar-systems", parallel = true)
  public static Object[][] tetrapolarSystems() {
    TetrapolarSystem ts = new TetrapolarSystem(1.0, 2.0, METRE);
    return new Object[][] {
        {ts, ts, true},
        {ts, new TetrapolarSystem(1000.0, 2000.0, MILLI(METRE)), true},
        {new TetrapolarSystem(1.0, 2.0, MILLI(METRE)), new TetrapolarSystem(1.0, 3.0, MILLI(METRE)), false}
    };
  }

  @DataProvider(name = "tetrapolar-systems-2", parallel = true)
  public static Object[][] tetrapolarSystems2() {
    return new Object[][] {
        {new TetrapolarSystem(0.030, 0.06, METRE), Quantities.getQuantity(1000, MILLI(OHM)), Math.PI * 9.0 / 400.0},
        {new TetrapolarSystem(30.0, 90.0, MILLI(METRE)), Quantities.getQuantity(1.0 / Math.PI, OHM), 3.0 / 50.0},
        {new TetrapolarSystem(40.0, 80.0, MILLI(METRE)), Quantities.getQuantity(1.0 / Math.PI, OHM), 3.0 / 100.0},
    };
  }

  @Test(dataProvider = "tetrapolar-systems")
  public static void testEquals(TetrapolarSystem system1, TetrapolarSystem system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, String.format("%s compared with %s", system1, system2));
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, String.format("%s compared with %s", system1, system2));
  }

  @Test
  public static void testNotEquals() {
    Assert.assertFalse(new TetrapolarSystem(1.0, 2.0, METRE).equals(new Object()));
  }

  @Test(dataProvider = "tetrapolar-systems-2")
  public static void testApparentResistivity(TetrapolarSystem system, Quantity<ElectricResistance> resistance,
                                             double specificResistance) {
    Assert.assertEquals(system.getApparent(resistance), specificResistance, 1.0e-6);
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testNotClone() throws CloneNotSupportedException {
    new TetrapolarSystem(1.0, 2.0, METRE).clone();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testInvalidConstructor() {
    new TetrapolarSystem(2.0, 1.0, METRE);
  }

  @DataProvider(name = "R(Ohm)-L(mm)")
  public static Object[][] rL() throws IOException {
    Supplier<DoubleStream> xVar = () -> doubleRange(1.0, 1000.0);
    xVar.get().mapToObj(value -> String.format("%.0f", value)).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL));

    Supplier<DoubleStream> yVar = () -> doubleRange(1.0, 100.0);
    yVar.get().mapToObj(value -> String.format("%.0f", value)).collect(
        new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL));
    return new Object[][] {{xVar, yVar}};
  }

  @Test(dataProvider = "R(Ohm)-L(mm)", enabled = false)
  public static void testApparent(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) {
    DoubleStream.of(1.0 / 3.0, 0.5).forEachOrdered(sToL -> {
      try {
        yVar.get().mapToObj(lmm -> xVar.get().map(r ->
            new TetrapolarSystem(lmm * sToL, lmm, MILLI(METRE)).getApparent(Quantities.getQuantity(r, OHM)))
        ).map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
            collect(new LineFileCollector(Paths.get(String.format("Apparent_Rho_At_%.2f.txt", sToL)),
                LineFileCollector.Direction.VERTICAL));
      }
      catch (IOException e) {
        Assert.fail(e.getMessage(), e);
      }
    });
  }

  private static DoubleStream doubleRange(double step, double end) {
    return DoubleStream.iterate(step, dl2L -> dl2L + step).
        limit(BigDecimal.valueOf(end / step).round(MathContext.UNLIMITED).intValue()).sequential();
  }
}