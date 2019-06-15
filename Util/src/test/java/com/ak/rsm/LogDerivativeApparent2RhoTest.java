package com.ak.rsm;

import java.util.Arrays;

import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class LogDerivativeApparent2RhoTest {
  private LogDerivativeApparent2RhoTest() {
  }

  @DataProvider(name = "waterDynamicParameters2")
  public static Object[][] waterDynamicParameters2() {
    return new Object[][] {
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(50.0, 30.0, MILLI(METRE)),
            },
            new double[] {0.7, Double.POSITIVE_INFINITY},
            Metrics.fromMilli(1.0),
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {0.7, Double.POSITIVE_INFINITY},
            Metrics.fromMilli(5.0),
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
            },
            new double[] {0.7, Double.POSITIVE_INFINITY},
            Metrics.fromMilli(5.0),
        },
    };
  }

  @Test(dataProvider = "waterDynamicParameters2")
  public static void testValue(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rho, double h) {
    double dh = Metrics.fromMilli(0.01);
    double logExpected = Arrays.stream(systems).mapToDouble(system -> {
      TrivariateFunction resistance2Layer = new Resistance2Layer(system);
      return StrictMath.log(Math.abs((resistance2Layer.value(rho[0], rho[1], h + dh) - resistance2Layer.value(rho[0], rho[1], h)) / dh));
    }).reduce((left, right) -> left - right).orElseThrow();
    double logActual = Arrays.stream(systems).mapToDouble(system ->
        new LogDerivativeApparent2Rho(system).value(Layers.getK12(rho[0], rho[1]), system.lToh(h))
    ).reduce((left, right) -> left - right).orElseThrow();
    Assert.assertEquals(logActual, logExpected, 0.001);
  }
}