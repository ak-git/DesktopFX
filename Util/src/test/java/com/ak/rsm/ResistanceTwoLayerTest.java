package com.ak.rsm;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.SimplexTest;
import com.ak.util.Metrics;
import com.ak.util.Strings;
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
        {new double[] {8.0, 1.0}, 10.0, 10.0, 20.0, 309.342},
        {new double[] {8.0, 1.0}, 10.0, 30.0, 90.0, 8.815},
        {new double[] {8.0, 1.0}, 50.0, 10.0, 20.0, 339.173},
        {new double[] {8.0, 1.0}, 50.0, 30.0, 90.0, 38.858},

        {new double[] {1.0, 1.0}, 0.0, 20.0, 40.0, 21.221},
        {new double[] {2.0, 2.0}, 0.0, 20.0, 40.0, 21.221 * 2.0},
        {new double[] {1.0, 1.0}, 0.0, 40.0, 80.0, 10.610},
        {new double[] {0.5, 0.5}, 0.0, 40.0, 80.0, 10.610 / 2.0},

        {new double[] {3.5, 1.35}, 10.0, 20.0, 40.0, 59.108},
        {new double[] {5.0, 2.0}, 15.0, 20.0, 40.0, 95.908},
        {new double[] {7.0, 1.0}, 20.0, 40.0, 80.0, 50.132},
        {new double[] {9.5, 0.5}, 30.0, 40.0, 80.0, 81.831},

        {new double[] {20.0, 1.0}, 1.0, 40.0, 80.0, 10.649},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0 - 10.0 / 200.0, 10.0, 30.0, 31.278},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0, 10.0, 30.0, 30.971},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0 - 10.0 / 200.0, 30.0, 50.0, 62.479},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0, 30.0, 50.0, 61.860},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0 - 10.0 / 200.0, 10.0, 50.0, 18.252},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0, 10.0, 50.0, 18.069},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0 - 10.0 / 200.0, 10.0, 30.0, 16.821},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0, 10.0, 30.0, 16.761},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0 - 10.0 / 200.0, 30.0, 50.0, 32.383},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0, 30.0, 50.0, 32.246},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0 - 10.0 / 200.0, 10.0, 50.0, 9.118},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0, 10.0, 50.0, 9.074},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0 - 10.0 / 200.0, 10.0, 30.0, 13.357},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0, 10.0, 30.0, 13.338},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0 - 10.0 / 200.0, 30.0, 50.0, 23.953},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0, 30.0, 50.0, 23.903},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0 - 10.0 / 200.0, 10.0, 50.0, 6.284},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0, 10.0, 50.0, 6.267},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0 - 10.0 / 200.0, 10.0, 30.0, 12.194},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0, 10.0, 30.0, 12.187},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0 - 10.0 / 200.0, 30.0, 50.0, 20.589},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0, 30.0, 50.0, 20.567},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0 - 10.0 / 200.0, 10.0, 50.0, 5.090},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0, 10.0, 50.0, 5.082},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0 - 10.0 / 200.0, 10.0, 30.0, 11.714},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0, 10.0, 30.0, 11.710},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0 - 10.0 / 200.0, 30.0, 50.0, 18.998},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0, 30.0, 50.0, 18.986},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0 - 10.0 / 200.0, 10.0, 50.0, 4.518},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0, 10.0, 50.0, 4.514},

        {new double[] {0.6973, 9.25}, 22.631 - 10.0 / 200.0, 10.0, 30.0, 11.714},
        {new double[] {0.6973, 9.25}, 22.631, 10.0, 30.0, 11.710},
        {new double[] {0.6973, 9.25}, 22.631 - 10.0 / 200.0, 30.0, 50.0, 18.999},
        {new double[] {0.6973, 9.25}, 22.631, 30.0, 50.0, 18.987},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0 - 10.0 / 200.0, 10.0, 30.0, 11.484},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0, 10.0, 30.0, 11.482},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0 - 10.0 / 200.0, 30.0, 50.0, 18.158},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0, 30.0, 50.0, 18.152},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0 - 10.0 / 200.0, 10.0, 50.0, 4.218},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0, 10.0, 50.0, 4.216},

        {new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0 - 10.0 / 200.0, 10.0, 30.0, 11.362},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0, 10.0, 30.0, 11.361},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0 - 10.0 / 200.0, 30.0, 50.0, 17.678},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0, 30.0, 50.0, 17.674},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0 - 10.0 / 200.0, 10.0, 50.0, 4.048},
        {new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0, 10.0, 50.0, 4.047},
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testLayer(double[] rho, double hmm, double smm, double lmm, double rOhm) {
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

  @DataProvider(name = "staticParameters")
  public static Object[][] staticParameters() {
    return new Object[][] {
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(7.0, 21.0, MILLI(METRE)),
                new TetrapolarSystem(21.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(7.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(14.0, 28.0, MILLI(METRE)),
                new TetrapolarSystem(28.0, 42.0, MILLI(METRE)),
            },
            new double[] {123.3, 176.1, 43.09, 170.14, 85.84 * 2}
        },
    };
  }

  @Test(dataProvider = "staticParameters", enabled = false)
  public static void testInverseStatic(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms) {
    ResistanceTwoLayer[] predicted = Stream.of(systems).map(ResistanceTwoLayer::new).toArray(ResistanceTwoLayer[]::new);
    SimpleBounds bounds = new SimpleBounds(
        new double[] {0.1, 0.1, Metrics.fromMilli(0.1)},
        new double[] {1000.0, 1000.0, Metrics.fromMilli(100.0)}
    );
    PointValuePair pair = SimplexTest.optimizeCMAES(point ->
            Inequality.proportional().applyAsDouble(rOhms, i -> predicted[i].value(point[0], point[1], point[2])),
        bounds);
    Logger.getAnonymousLogger().warning(toString3(pair));
  }

  @DataProvider(name = "dynamicParameters")
  public static Object[][] dynamicParameters() {
    return new Object[][] {
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(7.0, 21.0, MILLI(METRE)),
                new TetrapolarSystem(21.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(7.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(14.0, 28.0, MILLI(METRE)),
                new TetrapolarSystem(28.0, 42.0, MILLI(METRE)),
            },
            new double[] {123.3, 176.1, 43.09, 170.14, 85.84 * 2},
            new double[] {123.3 - 0.1, 176.1 - 0.125, 43.09 - 0.04, 170.14 - 0.16, 85.84 * 2 - 0.1 * 2},
            -Metrics.fromMilli(0.1)
        },
    };
  }

  @Test(dataProvider = "dynamicParameters", enabled = false)
  public static void testInverseDynamic(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
    TrivariateFunction[] predicted = Stream.of(systems).map(ResistanceTwoLayer::new).toArray(ResistanceTwoLayer[]::new);
    SimpleBounds bounds = new SimpleBounds(
        new double[] {0.1, 0.1, Metrics.fromMilli(0.1)},
        new double[] {1000.0, 1000.0, Metrics.fromMilli(100.0)}
    );
    PointValuePair pair = SimplexTest.optimizeCMAES(point -> {
          TrivariateFunction[] predictedDiff = Stream.of(systems).map(DerivativeRbyH::new).toArray(DerivativeRbyH[]::new);
          Inequality inequality = Inequality.proportional();
          inequality.applyAsDouble(rOhmsBefore, i -> predicted[i].value(point[0], point[1], point[2]));
          double[] dH = new double[rOhmsBefore.length];
          Arrays.setAll(dH, i -> (rOhmsAfter[i] - rOhmsBefore[i]) / dh);
          inequality.applyAsDouble(dH, i -> predictedDiff[i].value(point[0], point[1], point[2]));
          return inequality.getAsDouble();
        },
        bounds);
    Logger.getAnonymousLogger().warning(toString3(pair));
  }

  @DataProvider(name = "rho1-rho2-h")
  public static Object[][] dhParameters() {
    return new Object[][] {
        {10.0, 30.0, new double[] {30.971, 61.860}, new double[] {31.278, 62.479}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 5 mm
        {10.0, 50.0, new double[] {18.069, 61.860}, new double[] {18.252, 62.479}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 5 mm

        {10.0, 30.0, new double[] {16.761, 32.246}, new double[] {16.821, 32.383}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 10 mm
        {10.0, 50.0, new double[] {9.074, 32.246}, new double[] {9.118, 32.383}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 10 mm

        {10.0, 30.0, new double[] {13.338, 23.903}, new double[] {13.357, 23.953}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 15 mm
        {10.0, 50.0, new double[] {6.267, 23.903}, new double[] {6.284, 23.953}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 15 mm

        {10.0, 30.0, new double[] {12.187, 20.567}, new double[] {12.194, 20.589}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 20 mm
        {10.0, 50.0, new double[] {5.082, 20.567}, new double[] {5.090, 20.589}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 20 mm

        {10.0, 30.0, new double[] {11.710, 18.986}, new double[] {11.714, 18.998}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 25 mm
        {10.0, 50.0, new double[] {4.514, 18.986}, new double[] {4.518, 18.998}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 25 mm

        {10.0, 30.0, new double[] {11.482, 18.152}, new double[] {11.484, 18.158}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 30 mm
        {10.0, 50.0, new double[] {4.216, 18.152}, new double[] {4.218, 18.158}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 30 mm

        {10.0, 30.0, new double[] {11.361, 17.674}, new double[] {11.362, 17.678}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 35 mm
        {10.0, 50.0, new double[] {4.047, 17.674}, new double[] {4.048, 17.678}, -Metrics.fromMilli(10.0 / 200.0)}, // h == 35 mm
    };
  }

  @DataProvider(name = "rho1-rho2-h-experimental")
  public static Object[][] dhParameters2() {
    return new Object[][] {
        {10.0, 10.0 * 3, new double[] {29.44, 65.6}, new double[] {29.44 + 0.29, 65.6 + 0.695}, -Metrics.fromMilli(10.0 / 200.0)},// h = 5 mm
        {10.0, 10.0 * 3, new double[] {16.62, 33.8}, new double[] {16.62 + 0.07, 33.8 + 0.137}, -Metrics.fromMilli(10.0 / 200.0)},// h = 10 mm
        {10.0, 10.0 * 3, new double[] {13.154, 24.89}, new double[] {13.154 + 0.035, 24.89 + 0.05}, -Metrics.fromMilli(10.0 / 200.0)},// h = 15 mm
        {10.0, 10.0 * 3, new double[] {11.985, 21.3}, new double[] {11.985 + 0.007, 21.3 + 0.026}, -Metrics.fromMilli(10.0 / 200.0)},// h = 20 mm
        {10.0, 10.0 * 3, new double[] {11.45, 19.5}, new double[] {11.45 + 0.0055, 19.5 + 0.0145}, -Metrics.fromMilli(10.0 / 200.0)},// h = 25 mm
        {10.0, 10.0 * 3, new double[] {10.634, 17.7}, new double[] {10.634 + 0.021, 17.7 + 0.035}, -Metrics.fromMilli(10.0 / 200.0)},// h = 30 mm

        {10.0, 10.0 * 5, new double[] {19.62, 65.6}, new double[] {19.62 + 0.2, 65.6 + 0.695}, -Metrics.fromMilli(10.0 / 200.0)},// h = 5 mm
        {10.0, 10.0 * 5, new double[] {10.126, 33.8}, new double[] {10.126 + 0.0485, 33.8 + 0.137}, -Metrics.fromMilli(10.0 / 200.0)},// h = 10 mm
        {10.0, 10.0 * 5, new double[] {7.005, 24.89}, new double[] {7.005 + 0.0185, 24.89 + 0.05}, -Metrics.fromMilli(10.0 / 200.0)},// h = 15 mm
        {10.0, 10.0 * 5, new double[] {5.65, 21.3}, new double[] {5.65 + 0.011, 21.3 + 0.026}, -Metrics.fromMilli(10.0 / 200.0)},// h = 20 mm
        {10.0, 10.0 * 5, new double[] {4.956, 19.5}, new double[] {4.956 + 0.007, 19.5 + 0.0145}, -Metrics.fromMilli(10.0 / 200.0)},// h = 25 mm
        {10.0, 10.0 * 5, new double[] {4.432, 17.7}, new double[] {4.432 + 0.006, 17.7 + 0.035}, -Metrics.fromMilli(10.0 / 200.0)},// h = 30 mm

        {7.0, 7.0 * 3, new double[] {88.81, 141.1}, new double[] {88.81 - 0.04, 141.1 - 0.06}, -Metrics.fromMilli(0.1)},
        {7.0, 7.0 * 5, new double[] {34.58, 147.3}, new double[] {34.58 - 0.03, 147.3 - 0.06}, -Metrics.fromMilli(0.1)},
    };
  }

  @Test(dataProvider = "rho1-rho2-h", enabled = false)
  public static void testInverseDh2(@Nonnegative double sPUmm, @Nonnegative double lCCmm, @Nonnull double[] rOhmBefore, @Nonnull double[] rOhmAfter, double dHSI) {
    TetrapolarSystem systemSm = new TetrapolarSystem(sPUmm, lCCmm, MILLI(METRE));
    TetrapolarSystem systemBg = new TetrapolarSystem(sPUmm * 3.0, sPUmm * 5.0, MILLI(METRE));

    double rho1Apparent = systemSm.getApparent(rOhmBefore[0]);
    double rho2Apparent = systemBg.getApparent(rOhmBefore[1]);
    Logger.getAnonymousLogger().config(String.format("Apparent : %.3f; %.3f", rho1Apparent, rho2Apparent));

    TrivariateFunction predictedSm = new ResistanceTwoLayer(systemSm);
    TrivariateFunction predictedBg = new ResistanceTwoLayer(systemBg);

    SimpleBounds bounds = new SimpleBounds(
        new double[] {0.1, 0.1, Metrics.fromMilli(0.1)},
        new double[] {10.0, 1000.0, Metrics.fromMilli(sPUmm) * 10.0}
    );

    PointValuePair pointValuePair = SimplexTest.optimizeCMAES(point -> {
      double rho1 = point[0];
      double rho2 = point[1];
      double h = point[2];

      TrivariateFunction predictedDiffSm = new DerivativeRbyH(systemSm);
      TrivariateFunction predictedDiffBg = new DerivativeRbyH(systemBg);

      Inequality inequality = Inequality.expAndLogDifference();
      inequality.applyAsDouble(rOhmBefore[0], predictedSm.value(rho1, rho2, h));
      inequality.applyAsDouble(rOhmBefore[1], predictedBg.value(rho1, rho2, h));
      inequality.applyAsDouble((rOhmAfter[0] - rOhmBefore[0]) / dHSI, predictedDiffSm.value(rho1, rho2, h));
      inequality.applyAsDouble((rOhmAfter[1] - rOhmBefore[1]) / dHSI, predictedDiffBg.value(rho1, rho2, h));
      return inequality.getAsDouble();
    }, bounds);

    double rho1 = pointValuePair.getPoint()[0];
    double rho2 = pointValuePair.getPoint()[1];
    double h = pointValuePair.getPoint()[2];
    String validate = String.format("R1 = %.1f (%s = %.3f), R2 = %.1f (%s = %.3f)",
        predictedSm.value(rho1, rho2, h), Strings.CAP_DELTA, predictedSm.value(rho1, rho2, h + dHSI) - predictedSm.value(rho1, rho2, h),
        predictedBg.value(rho1, rho2, h), Strings.CAP_DELTA, predictedBg.value(rho1, rho2, h + dHSI) - predictedBg.value(rho1, rho2, h));

    Logger.getAnonymousLogger().warning(String.format("%.0f x %.0f / %.0f x %.0f mm, %sh = %.3f mm, %s, error = %.6f, %s", sPUmm, lCCmm, sPUmm * 3.0, sPUmm * 5.0,
        Strings.CAP_DELTA, dHSI * 1000, toString3(pointValuePair), pointValuePair.getValue(), validate));
  }

  private static String toString3(@Nonnull PointValuePair point) {
    double[] v = point.getPoint();
    return String.format("%s1 = %.3f, %s2 = %.3f, h = %.3f mm, e = %.6f", Strings.RHO, v[0], Strings.RHO, v[1], v[2] * 1000, point.getValue());
  }
}