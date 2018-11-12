package com.ak.rsm;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

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
        {new Double[] {1.0, 1.0}, 0.0, 20.0, 40.0, 21.221},
        {new Double[] {2.0, 2.0}, 0.0, 20.0, 40.0, 21.221 * 2.0},
        {new Double[] {1.0, 1.0}, 0.0, 40.0, 80.0, 10.610},
        {new Double[] {0.5, 0.5}, 0.0, 40.0, 80.0, 10.610 / 2.0},

        {new Double[] {3.5, 1.35}, 10.0, 20.0, 40.0, 59.108},
        {new Double[] {5.0, 2.0}, 15.0, 20.0, 40.0, 95.908},
        {new Double[] {7.0, 1.0}, 20.0, 40.0, 80.0, 50.132},
        {new Double[] {9.5, 0.5}, 30.0, 40.0, 80.0, 81.831},

        {new Double[] {20.0, 1.0}, 1.0, 40.0, 80.0, 10.649},

        {new Double[] {0.746, Double.POSITIVE_INFINITY}, 10.0, 10.0, 50.0, 9.670},
        {new Double[] {0.746, Double.POSITIVE_INFINITY}, 10.0, 30.0, 50.0, 34.365},
        {new Double[] {0.746, Double.POSITIVE_INFINITY}, 10.0, 10.0, 30.0, 17.862},
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
  public static void testInverseRho1(@Nonnegative double hmm, @Nonnegative double sPUmm,
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

  @DataProvider(name = "rho1-h")
  public static Object[][] rho1HParameters() {
    return new Object[][] {
        {10.0, new double[] {33.860, 9.822}, new double[] {33.682, 9.725}, new double[] {0.612, Metrics.fromMilli(8.0)}},
    };
  }

  @Test(dataProvider = "rho1-h")
  public static void testInverseRho1H(@Nonnegative double sPUmm, @Nonnull double[] rOhmBefore, @Nonnull double[] rOhmAfter,
                                      @Nonnull double[] rho1hExpected) {
    TetrapolarSystem systemBig = new TetrapolarSystem(sPUmm * 3, sPUmm * 5.0, MILLI(METRE));
    TetrapolarSystem systemSmall = new TetrapolarSystem(sPUmm, sPUmm * 5.0, MILLI(METRE));

    double rho1Apparent = systemBig.getApparent(rOhmBefore[0]);
    double rho2Apparent = systemSmall.getApparent(rOhmBefore[1]);
    Logger.getAnonymousLogger().config(String.format("Apparent : %.3f; %.3f", rho1Apparent, rho2Apparent));

    SimpleBounds bounds = new SimpleBounds(
        new double[] {0.0, 0.0},
        new double[] {rho1Apparent, Metrics.fromMilli(sPUmm * 5.0 / 2.0)}
    );

    TrivariateFunction predictedSmall = new ResistanceTwoLayer(systemSmall);
    TrivariateFunction predictedBig = new ResistanceTwoLayer(systemBig);

    double[] point = SimplexTest.optimizeCMAES(rho1h -> {
          Inequality inequality = Inequality.log1pDifference();
          inequality.applyAsDouble(rOhmBefore[0], predictedBig.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1]));
          inequality.applyAsDouble(rOhmBefore[1], predictedSmall.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1]));

          double dH = Metrics.fromMilli(0.05);
          inequality.applyAsDouble(rOhmBefore[0] - rOhmAfter[0],
              predictedBig.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1]) -
                  predictedBig.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1] + dH)
          );
          inequality.applyAsDouble(rOhmBefore[1] - rOhmAfter[1],
              predictedSmall.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1]) -
                  predictedSmall.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1] + dH)
          );
          return inequality.getAsDouble();
        },
        bounds, bounds.getUpper(), new double[] {rho1Apparent / 10.0, Metrics.fromMilli(sPUmm / 10.0)}
    ).getPoint();
    Assert.assertEquals(point, rho1hExpected, 1.0e-3, Arrays.toString(point));
  }

  @DataProvider(name = "rho1-h-dH")
  public static Object[][] rho1HdHParameters() {
    return new Object[][] {
        {10.0, new double[] {33.860, 9.822}, new double[] {33.682, 9.725}, new double[] {0.62210, Metrics.fromMilli(8.22), Metrics.fromMilli(0.05)}},
    };
  }

  @Test(dataProvider = "rho1-h-dH")
  public static void testInverseRho1HdH(@Nonnegative double sPUmm, @Nonnull double[] rOhmBefore, @Nonnull double[] rOhmAfter,
                                        @Nonnull double[] rho1hExpected) {
    TetrapolarSystem systemBig = new TetrapolarSystem(sPUmm * 3, sPUmm * 5.0, MILLI(METRE));
    TetrapolarSystem systemSmall = new TetrapolarSystem(sPUmm, sPUmm * 5.0, MILLI(METRE));

    double rho1Apparent = systemBig.getApparent(rOhmBefore[0]);
    double rho2Apparent = systemSmall.getApparent(rOhmBefore[1]);
    Logger.getAnonymousLogger().config(String.format("Apparent : %.3f; %.3f", rho1Apparent, rho2Apparent));

    SimpleBounds bounds = new SimpleBounds(
        new double[] {0.0, 0.0, 0.0},
        new double[] {rho1Apparent, Metrics.fromMilli(sPUmm * 5.0 / 2.0), Metrics.fromMilli(1.0)}
    );

    TrivariateFunction predictedSmall = new ResistanceTwoLayer(systemSmall);
    TrivariateFunction predictedBig = new ResistanceTwoLayer(systemBig);

    double[] point = SimplexTest.optimizeCMAES(rho1h -> {
          Inequality inequality = Inequality.log1pDifference();
          inequality.applyAsDouble(rOhmBefore[0], predictedBig.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1]));
          inequality.applyAsDouble(rOhmBefore[1], predictedSmall.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1]));

          double dH = rho1h[2];
          inequality.applyAsDouble(rOhmBefore[0] - rOhmAfter[0],
              predictedBig.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1]) -
                  predictedBig.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1] + dH)
          );
          inequality.applyAsDouble(rOhmBefore[1] - rOhmAfter[1],
              predictedSmall.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1]) -
                  predictedSmall.value(rho1h[0], Double.POSITIVE_INFINITY, rho1h[1] + dH)
          );
          return inequality.getAsDouble();
        },
        bounds, bounds.getUpper(), new double[] {rho1Apparent / 10.0, Metrics.fromMilli(sPUmm / 10.0), Metrics.fromMilli(0.01)}
    ).getPoint();
    Assert.assertEquals(point, rho1hExpected, 1.0e-5, Arrays.toString(point));
  }

  @DataProvider(name = "rho1-rho2-h-dRho2")
  public static Object[][] dRho2Parameters() {
    return new Object[][] {
        {10.0, new double[] {163.0, 36.0}, new double[] {166.5, 36.7}},
    };
  }

  @Test(dataProvider = "rho1-rho2-h-dRho2", enabled = false)
  public static void testInverseDRho2(@Nonnegative double sPUmm, @Nonnull double[] rOhmBefore, @Nonnull double[] rOhmAfter) {
    TetrapolarSystem systemBig = new TetrapolarSystem(sPUmm * 3, sPUmm * 5.0, MILLI(METRE));
    TetrapolarSystem systemSmall = new TetrapolarSystem(sPUmm, sPUmm * 5.0, MILLI(METRE));

    double rho1Apparent = systemBig.getApparent(rOhmBefore[0]);
    double rho2Apparent = systemSmall.getApparent(rOhmBefore[1]);
    Logger.getAnonymousLogger().info(String.format("Apparent : %.3f; %.3f", rho1Apparent, rho2Apparent));

    SimpleBounds bounds = new SimpleBounds(
        new double[] {rho1Apparent, 0.0},
        new double[] {Double.POSITIVE_INFINITY, rho2Apparent}
    );

    TrivariateFunction predictedSmall = new ResistanceTwoLayer(systemSmall);
    TrivariateFunction predictedBig = new ResistanceTwoLayer(systemBig);

    DoubleStream.iterate(0.8, hmm -> hmm + 0.001).takeWhile(hmm -> hmm < 0.9).forEachOrdered(hmm -> {
      double[] point = SimplexTest.optimizeCMAES(v -> {
            Inequality inequality = Inequality.log1pDifference();
            inequality.applyAsDouble(rOhmBefore[0], predictedBig.value(v[0], v[1], Metrics.fromMilli(hmm)));
            inequality.applyAsDouble(rOhmBefore[1], predictedSmall.value(v[0], v[1], Metrics.fromMilli(hmm)));
            return inequality.getAsDouble();
          },
          bounds, new double[] {rho1Apparent, rho2Apparent},
          new double[] {rho1Apparent / 10.0, rho2Apparent / 10.0}
      ).getPoint();

      PointValuePair point2 = SimplexTest.optimizeNelderMead(v -> {
            Inequality inequality = Inequality.log1pDifference();
            inequality.applyAsDouble(rOhmAfter[0] - rOhmBefore[0],
                predictedBig.value(point[0], point[1] + v[0], Metrics.fromMilli(hmm)) -
                    predictedBig.value(point[0], point[1], Metrics.fromMilli(hmm)));
            inequality.applyAsDouble(rOhmAfter[1] - rOhmBefore[1],
                predictedSmall.value(point[0], point[1] + v[0], Metrics.fromMilli(hmm)) -
                    predictedSmall.value(point[0], point[1], Metrics.fromMilli(hmm)));
            return inequality.getAsDouble();
          },
          new double[] {rho2Apparent / 10.0},
          new double[] {rho1Apparent / 100.0}
      );

      Logger.getAnonymousLogger().info(String.format("h = %.3f mm, rho = %s, dRho2 = %.4f, e = %.6f",
          hmm, toString(point), point2.getPoint()[0], point2.getValue()));
    });
  }

  private static String toString(@Nonnull double[] v) {
    return Arrays.stream(v).mapToObj(value -> String.format("%.4f", value)).collect(Collectors.joining(", ", "[", "]"));
  }
}