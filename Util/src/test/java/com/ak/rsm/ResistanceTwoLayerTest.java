package com.ak.rsm;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.SimplexTest;
import com.ak.util.Metrics;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
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
        {new Double[] {8.0, 1.0}, 10.0, 10.0, 20.0, 309.342},
        {new Double[] {8.0, 1.0}, 10.0, 30.0, 90.0, 8.815},
        {new Double[] {8.0, 1.0}, 50.0, 10.0, 20.0, 339.173},
        {new Double[] {8.0, 1.0}, 50.0, 30.0, 90.0, 38.858},

        {new Double[] {1.0, 1.0}, 0.0, 20.0, 40.0, 21.221},
        {new Double[] {2.0, 2.0}, 0.0, 20.0, 40.0, 21.221 * 2.0},
        {new Double[] {1.0, 1.0}, 0.0, 40.0, 80.0, 10.610},
        {new Double[] {0.5, 0.5}, 0.0, 40.0, 80.0, 10.610 / 2.0},

        {new Double[] {3.5, 1.35}, 10.0, 20.0, 40.0, 59.108},
        {new Double[] {5.0, 2.0}, 15.0, 20.0, 40.0, 95.908},
        {new Double[] {7.0, 1.0}, 20.0, 40.0, 80.0, 50.132},
        {new Double[] {9.5, 0.5}, 30.0, 40.0, 80.0, 81.831},

        {new Double[] {20.0, 1.0}, 1.0, 40.0, 80.0, 10.649},

        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 5.0 - 10.0 / 200.0, 10.0, 30.0, 31.278},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 5.0, 10.0, 30.0, 30.971},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 5.0 - 10.0 / 200.0, 30.0, 50.0, 62.479},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 5.0, 30.0, 50.0, 61.860},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 5.0 - 10.0 / 200.0, 10.0, 50.0, 18.252},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 5.0, 10.0, 50.0, 18.069},

        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 10.0 - 10.0 / 200.0, 10.0, 30.0, 16.821},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 10.0, 10.0, 30.0, 16.761},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 10.0 - 10.0 / 200.0, 30.0, 50.0, 32.383},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 10.0, 30.0, 50.0, 32.246},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 10.0 - 10.0 / 200.0, 10.0, 50.0, 9.118},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 10.0, 10.0, 50.0, 9.074},

        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 15.0 - 10.0 / 200.0, 10.0, 30.0, 13.357},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 15.0, 10.0, 30.0, 13.338},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 15.0 - 10.0 / 200.0, 30.0, 50.0, 23.953},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 15.0, 30.0, 50.0, 23.903},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 15.0 - 10.0 / 200.0, 10.0, 50.0, 6.284},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 15.0, 10.0, 50.0, 6.267},

        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 20.0 - 10.0 / 200.0, 10.0, 30.0, 12.194},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 20.0, 10.0, 30.0, 12.187},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 20.0 - 10.0 / 200.0, 30.0, 50.0, 20.589},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 20.0, 30.0, 50.0, 20.567},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 20.0 - 10.0 / 200.0, 10.0, 50.0, 5.090},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 20.0, 10.0, 50.0, 5.082},

        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 25.0 - 10.0 / 200.0, 10.0, 30.0, 11.714},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 25.0, 10.0, 30.0, 11.710},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 25.0 - 10.0 / 200.0, 30.0, 50.0, 18.998},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 25.0, 30.0, 50.0, 18.986},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 25.0 - 10.0 / 200.0, 10.0, 50.0, 4.518},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 25.0, 10.0, 50.0, 4.514},

        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 30.0 - 10.0 / 200.0, 10.0, 30.0, 11.484},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 30.0, 10.0, 30.0, 11.482},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 30.0 - 10.0 / 200.0, 30.0, 50.0, 18.158},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 30.0, 30.0, 50.0, 18.152},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 30.0 - 10.0 / 200.0, 10.0, 50.0, 4.218},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 30.0, 10.0, 50.0, 4.216},

        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 35.0 - 10.0 / 200.0, 10.0, 30.0, 11.362},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 35.0, 10.0, 30.0, 11.361},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 35.0 - 10.0 / 200.0, 30.0, 50.0, 17.678},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 35.0, 30.0, 50.0, 17.674},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 35.0 - 10.0 / 200.0, 10.0, 50.0, 4.048},
        {new Double[] {0.7, Double.POSITIVE_INFINITY}, 35.0, 10.0, 50.0, 4.047},
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
        {10.0, 10.0, new double[] {16.17, 33.15}, 0.697},
    };
  }

  @Test(dataProvider = "rho1")
  public static void testInverseRho1(@Nonnegative double hmm, @Nonnegative double sPUmm,
                                     @Nonnull double[] rOhmActual, @Nonnegative double rho1Expected) {
    TetrapolarSystem electrodeSystemSmall = new TetrapolarSystem(sPUmm, sPUmm * 3.0, MILLI(METRE));
    TetrapolarSystem electrodeSystemBig = new TetrapolarSystem(sPUmm * 3.0, sPUmm * 5.0, MILLI(METRE));

    PointValuePair pointValuePair = SimplexTest.optimizeNelderMead(new MultivariateFunction() {
      private final TrivariateFunction resistancePredictedSmall = new ResistanceTwoLayer(electrodeSystemSmall);
      private final TrivariateFunction resistancePredictedBig = new ResistanceTwoLayer(electrodeSystemBig);

      @Override
      public double value(double[] rho1) {
        Inequality inequality = Inequality.logDifference();
        inequality.applyAsDouble(rOhmActual[0], resistancePredictedSmall.value(rho1[0], Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)));
        inequality.applyAsDouble(rOhmActual[1], resistancePredictedBig.value(rho1[0], Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)));
        return inequality.getAsDouble();
      }
    }, new double[] {1.0}, new double[] {0.001});
    Assert.assertEquals(pointValuePair.getPoint()[0], rho1Expected, 1.0e-3);
  }

  @DataProvider(name = "rho1-h")
  public static Object[][] rho1HParameters() {
    return new Object[][] {
        {10.0, new double[] {16.17, 33.15}, new double[] {16.09, 33.0}, new double[] {0.624, Metrics.fromMilli(8.7)}},
    };
  }

  @Test(dataProvider = "rho1-h")
  public static void testInverseRho1H(@Nonnegative double sPUmm, @Nonnull double[] rOhmBefore, @Nonnull double[] rOhmAfter,
                                      @Nonnull double[] rho1hExpected) {
    TetrapolarSystem systemSmall = new TetrapolarSystem(sPUmm, sPUmm * 3.0, MILLI(METRE));
    TetrapolarSystem systemBig = new TetrapolarSystem(sPUmm * 3, sPUmm * 5.0, MILLI(METRE));

    double rho1Apparent = systemSmall.getApparent(rOhmBefore[0]);
    double rho2Apparent = systemBig.getApparent(rOhmBefore[1]);
    Logger.getAnonymousLogger().config(String.format("Apparent : %.3f; %.3f", rho1Apparent, rho2Apparent));

    SimpleBounds bounds = new SimpleBounds(
        new double[] {0.0, 0.0},
        new double[] {rho1Apparent, Metrics.fromMilli(sPUmm * 5.0 / 2.0)}
    );

    TrivariateFunction predictedSmall = new ResistanceTwoLayer(systemSmall);
    TrivariateFunction predictedBig = new ResistanceTwoLayer(systemBig);

    double[] point = SimplexTest.optimizeCMAES(rho1h -> {
          Inequality inequality = Inequality.log1pDifference();
          inequality.applyAsDouble(rOhmBefore[0], predictedSmall.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1]));
          inequality.applyAsDouble(rOhmBefore[1], predictedBig.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1]));

          double dH = Metrics.fromMilli(0.05);
          inequality.applyAsDouble(rOhmBefore[0] - rOhmAfter[0],
              predictedSmall.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1]) -
                  predictedSmall.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1] + dH)
          );
          inequality.applyAsDouble(rOhmBefore[1] - rOhmAfter[1],
              predictedBig.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1]) -
                  predictedBig.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1] + dH)
          );
          return inequality.getAsDouble();
        },
        bounds, bounds.getUpper(), new double[] {rho1Apparent / 10.0, Metrics.fromMilli(sPUmm / 10.0)}
    ).getPoint();
    Assert.assertEquals(point, rho1hExpected, 1.0e-3, Arrays.toString(point));
  }


  @DataProvider(name = "rho1-rho2-h-dh")
  public static Object[][] dhParameters() {
    return new Object[][] {
        {6.0, new double[] {80.0, 224.3}, new double[] {80.1, 224.4}},
        {6.0, new double[] {92.1, 221.95}, new double[] {92.15, 222.0}},
        {8.0, new double[] {143.55, 212.3}, new double[] {143.60, 212.35}},
    };
  }

  @Test(dataProvider = "rho1-rho2-h-dh", enabled = false)
  public static void testInverseDh(@Nonnegative double sPUmm, @Nonnull double[] rOhmBefore, @Nonnull double[] rOhmAfter) {
    TetrapolarSystem systemSmall = new TetrapolarSystem(sPUmm, sPUmm * 3.0, MILLI(METRE));
    TetrapolarSystem systemBig = new TetrapolarSystem(sPUmm * 3.0, sPUmm * 5.0, MILLI(METRE));

    double rho1Apparent = systemSmall.getApparent(rOhmBefore[0]);
    double rho2Apparent = systemBig.getApparent(rOhmBefore[1]);
    Logger.getAnonymousLogger().info(String.format("Apparent : %.3f; %.3f", rho1Apparent, rho2Apparent));

    TrivariateFunction predictedSmall = new ResistanceTwoLayer(systemSmall);
    TrivariateFunction predictedBig = new ResistanceTwoLayer(systemBig);

    SimpleBounds bounds;
    if (rho1Apparent > rho2Apparent) {
      bounds = new SimpleBounds(
          new double[] {rho1Apparent, 0.0, 0.0},
          new double[] {Double.POSITIVE_INFINITY, rho2Apparent, Metrics.fromMilli(sPUmm * 3.0)}
      );
    }
    else {
      bounds = new SimpleBounds(
          new double[] {0.0, rho2Apparent, 0.0},
          new double[] {rho1Apparent, Double.POSITIVE_INFINITY, Metrics.fromMilli(sPUmm * 3.0)}
      );
    }

    PointValuePair pointValuePair = SimplexTest.optimizeCMAES(point -> {
          double rho1 = point[0];
          double rho2 = Double.POSITIVE_INFINITY;
          double h = point[2];
          double dh = Metrics.fromMilli(0.05);

          Inequality inequality = Inequality.log1pDifference();
          inequality.applyAsDouble(rOhmBefore[0], predictedSmall.value(rho1, rho2, h));
          inequality.applyAsDouble(rOhmBefore[1], predictedBig.value(rho1, rho2, h));
          inequality.applyAsDouble(rOhmAfter[0] - rOhmBefore[0],
              predictedSmall.value(rho1, rho2, h - dh) - predictedSmall.value(rho1, rho2, h));
          inequality.applyAsDouble(rOhmAfter[1] - rOhmBefore[1],
              predictedBig.value(rho1, rho2, h - dh) - predictedBig.value(rho1, rho2, h));
          return inequality.getAsDouble();
        }, bounds, new double[] {rho1Apparent, rho2Apparent, Metrics.fromMilli(sPUmm * 2.0)},
        new double[] {rho1Apparent / 10.0, rho2Apparent / 10.0, Metrics.fromMilli(0.1)});
    Logger.getAnonymousLogger().info(toString(pointValuePair.getPoint()));
  }

  private static String toString(@Nonnull double[] v) {
    return Arrays.stream(v).mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining(", ", "[", "]"));
  }
}