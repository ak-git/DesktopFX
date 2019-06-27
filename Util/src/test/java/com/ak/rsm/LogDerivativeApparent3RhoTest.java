package com.ak.rsm;

import java.util.Arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class LogDerivativeApparent3RhoTest {
  private LogDerivativeApparent3RhoTest() {
  }

  @DataProvider(name = "waterDynamicParameters3")
  public static Object[][] waterDynamicParameters3() {
    return new Object[][] {
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(50.0, 30.0, MILLI(METRE)),
            },
            new double[] {10.0, 1.0, 2.0},
            Metrics.fromMilli(0.1), new int[] {10, 1}
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(50.0, 30.0, MILLI(METRE)),
            },
            new double[] {1.0, 10.0, 2.0},
            Metrics.fromMilli(0.1), new int[] {1, 10}
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(50.0, 30.0, MILLI(METRE)),
            },
            new double[] {1.0, 2.0, 10.0},
            Metrics.fromMilli(0.1), new int[] {10, 10}
        },
    };
  }

  @Test(dataProvider = "waterDynamicParameters3")
  public static void testValue(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rho, @Nonnegative double h, @Nonnegative int[] p) {
    int dh = 1;
    double logExpected = Arrays.stream(systems).mapToDouble(system -> {
      Resistance3Layer resistance3Layer = new Resistance3Layer(system, h);
      return StrictMath.log(
          Math.abs(
              (resistance3Layer.value(rho[0], rho[1], rho[2], p[0] + dh, p[1] + dh) - resistance3Layer.value(rho[0], rho[1], rho[2], p[0], p[1])) / (h * dh)
          )
      );
    }).reduce((left, right) -> left - right).orElseThrow();
    double logActual = Arrays.stream(systems).mapToDouble(system ->
        new LogDerivativeApparent3Rho(system.sToL(), 0.03 / h).value(Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2]), p[0], p[1])
    ).reduce((left, right) -> left - right).orElseThrow();
    Assert.assertEquals(logActual, logExpected, 0.1);
  }
}