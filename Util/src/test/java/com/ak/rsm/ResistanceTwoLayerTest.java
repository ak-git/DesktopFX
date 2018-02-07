package com.ak.rsm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import com.ak.inverse.Inequality;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public class ResistanceTwoLayerTest {
  private ResistanceTwoLayerTest() {
  }

  @DataProvider(name = "layer-model", parallel = true)
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {new Double[] {1.0, 1.0}, 0.0, 20.0, 40.0, 21.221},
        {new Double[] {2.0, 2.0}, 0.0, 20.0, 40.0, 21.221 * 2.0},
        {new Double[] {1.0, 1.0}, 0.0, 40.0, 80.0, 10.610},
        {new Double[] {0.5, 0.5}, 0.0, 40.0, 80.0, 10.610 / 2.0},

        {new Double[] {3.5, 1.35}, 10.0, 20.0, 40.0, 59.108},
        {new Double[] {5.0, 2.0}, 15.0, 20.0, 40.0, 95.908},
        {new Double[] {7.0, 1.0}, 20.0, 40.0, 80.0, 50.132},
        {new Double[] {9.5, 0.5}, 30.0, 40.0, 80.0, 81.831},

        {new Double[] {20.0, 1.0}, 1.0, 40.0, 80.0, 10.649}
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testLayer(Double[] rho, double hmm, double smm, double lmm, double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MetricPrefix.MILLI(Units.METRE));
    Assert.assertEquals(new ResistanceTwoLayer(system).value(rho[0], rho[1],
        Quantities.getQuantity(hmm, MetricPrefix.MILLI(Units.METRE)).to(Units.METRE).getValue().doubleValue()), rOhm, 0.001);
  }

  @Test
  public static void testRho1ToRho2() {
    Assert.assertEquals(ResistanceTwoLayer.getRho1ToRho2(1.0), 0.0, Float.MIN_NORMAL);
    Assert.assertEquals(ResistanceTwoLayer.getRho1ToRho2(0.0), 1.0, Float.MIN_NORMAL);
    Assert.assertEquals(ResistanceTwoLayer.getRho1ToRho2(-1.0), Double.POSITIVE_INFINITY, Float.MIN_NORMAL);
    Assert.assertEquals(ResistanceTwoLayer.getRho1ToRho2(0.1), ResistanceTwoLayer.getK12(0.1, 1.0), Float.MIN_NORMAL);
    Assert.assertEquals(ResistanceTwoLayer.getRho1ToRho2(10.0), ResistanceTwoLayer.getK12(10.0, 1.0), Float.MIN_NORMAL);
    Assert.assertEquals(ResistanceTwoLayer.getK12(10.0, Double.POSITIVE_INFINITY), 1.0, Float.MIN_NORMAL);
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testNotClone() throws CloneNotSupportedException {
    new ResistanceTwoLayer(new TetrapolarSystem(1, 2, MetricPrefix.MILLI(Units.METRE))).clone();
  }


  @DataProvider(name = "two-measures")
  public static Object[][] twoMeasuresParameters() {
    return new Object[][] {
        {new double[] {10.0, 1.0}, 15.0},
        {new double[] {10.0, 1.0}, 15.01},
    };
  }

  @Test(dataProvider = "two-measures", enabled = false)
  public static void testTwoMeasures(double[] rho, double hmm) {
    double lCCmm = 50.0;
    double sPU1mm = 30.0;
    double sPU2mm = 10.0;
    double dLmm = 0.05;

    double[] rOhmsMeans = DoubleStream.of(dLmm, -dLmm)
        .mapToObj(error -> Arrays.stream(new double[] {sPU1mm - error, sPU2mm - error}).map(s -> {
          TetrapolarSystem tetrapolarSystem = new TetrapolarSystem(s, lCCmm + error, MetricPrefix.MILLI(Units.METRE));
          return new ResistanceTwoLayer(tetrapolarSystem).value(rho[0], rho[1],
              Quantities.getQuantity(hmm, MetricPrefix.MILLI(Units.METRE)).to(Units.METRE).getValue().doubleValue());
        }).toArray())
        .max(Comparator.comparingDouble(new ToDoubleFunction<>() {
          double[] rOhmsPredicted = DoubleStream.of(sPU1mm, sPU2mm).map(s -> {
            TetrapolarSystem tetrapolarSystem = new TetrapolarSystem(s, lCCmm, MetricPrefix.MILLI(Units.METRE));
            return new ResistanceTwoLayer(tetrapolarSystem).value(rho[0], rho[1],
                Quantities.getQuantity(hmm, MetricPrefix.MILLI(Units.METRE)).to(Units.METRE).getValue().doubleValue());
          }).toArray();

          @Override
          public double applyAsDouble(double[] rOhms) {
            Inequality inequality = Inequality.proportional();
            for (int i = 0; i < rOhms.length; i++) {
              inequality.applyAsDouble(rOhms[i], rOhmsPredicted[i]);
            }
            return inequality.getAsDouble();
          }
        }))
        .orElseThrow(IllegalStateException::new);

    Logger.getAnonymousLogger().
        info(DoubleStream.of(rOhmsMeans).mapToObj(value -> String.format("%.3f", value)).collect(Collectors.joining(", ")));
  }
}