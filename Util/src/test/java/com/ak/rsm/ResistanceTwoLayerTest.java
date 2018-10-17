package com.ak.rsm;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;

import com.ak.inverse.Inequality;
import com.ak.math.SimplexTest;
import com.ak.util.Metrics;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class ResistanceTwoLayerTest {
  private ResistanceTwoLayerTest() {
  }

  @DataProvider(name = "layer-model")
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

        {new Double[] {20.0, 1.0}, 1.0, 40.0, 80.0, 10.649},
        {new Double[] {8.0, 1.0}, 50.0, 30.0, 90.0, 38.858}
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testLayer(Double[] rho, double hmm, double smm, double lmm, double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    Assert.assertEquals(new ResistanceTwoLayer(system).value(rho[0], rho[1], Metrics.fromMilli(hmm)), rOhm, 0.001);
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
    new ResistanceTwoLayer(new TetrapolarSystem(1, 2, MILLI(METRE))).clone();
  }

  @DataProvider(name = "rho1")
  public static Object[][] rho1Parameters() {
    return new Object[][] {
        {10.0, 10.0, 50.0, 9.616,
            0.74},
        {10.0, 30.0, 50.0, 33.666,
            0.73},

        {10.0, 10.0, 50.0, 9.782,
            0.76},
        {10.0, 30.0, 50.0, 33.669,
            0.73},
    };
  }

  @Test(dataProvider = "rho1")
  public void testInverseRho1(@Nonnegative double hmm, @Nonnegative double sPUmm, @Nonnegative double lCCmm,
                              @Nonnegative double rOhmActual, @Nonnegative double rho1Expected) {
    TetrapolarSystem electrodeSystem = new TetrapolarSystem(sPUmm, lCCmm, MILLI(METRE));

    ToDoubleFunction<TetrapolarSystem> findRho1 = electrode -> SimplexTest.optimizeNelderMead(new MultivariateFunction() {
      private final TrivariateFunction resistancePredicted = new ResistanceTwoLayer(electrode);

      @Override
      public double value(double[] rho1) {
        return Inequality.logDifference().applyAsDouble(rOhmActual, resistancePredicted.value(rho1[0], Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)));
      }
    }, new double[] {electrodeSystem.getApparent(rOhmActual)}, new double[] {0.001}).getPoint()[0];

    double[] doubles = Arrays.stream(electrodeSystem.newWithError(0.1, MILLI(METRE))).mapToDouble(findRho1).toArray();
    double center = findRho1.applyAsDouble(electrodeSystem);
    Logger.getAnonymousLogger().config(String.format("%.3f +%.3f/-%.3f", center, doubles[0] - center, center - doubles[1]));
    Assert.assertEquals(center, rho1Expected, 0.01);
  }
}