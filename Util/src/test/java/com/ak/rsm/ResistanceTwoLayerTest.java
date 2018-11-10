package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.SimplexTest;
import com.ak.util.Metrics;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
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
        {10.0, 10.0, new double[] {33.860, 9.822}, 0.746},
    };
  }

  @Test(dataProvider = "rho1")
  public void testInverseRho1(@Nonnegative double hmm, @Nonnegative double sPUmm,
                              @Nonnull double[] rOhmActual, @Nonnegative double rho1Expected) {
    TetrapolarSystem electrodeSystemBig = new TetrapolarSystem(sPUmm * 3.0, sPUmm * 5.0, MILLI(METRE));
    TetrapolarSystem electrodeSystemSmall = new TetrapolarSystem(sPUmm, sPUmm * 5.0, MILLI(METRE));

    PointValuePair pointValuePair = SimplexTest.optimizeNelderMead(new MultivariateFunction() {
      private final TrivariateFunction resistancePredictedBig = new ResistanceTwoLayer(electrodeSystemBig);
      private final TrivariateFunction resistancePredictedSmall = new ResistanceTwoLayer(electrodeSystemSmall);

      @Override
      public double value(double[] rho1) {
        Inequality inequality = Inequality.logDifference();
        inequality.applyAsDouble(rOhmActual[0], resistancePredictedBig.value(rho1[0], Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)));
        inequality.applyAsDouble(rOhmActual[1], resistancePredictedSmall.value(rho1[0], Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)));
        return inequality.getAsDouble();
      }
    }, new double[] {1.0}, new double[] {0.001});
    Assert.assertEquals(pointValuePair.getPoint()[0], rho1Expected, 1.0e-3);
  }
}