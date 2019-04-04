package com.ak.rsm;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.SimplexTest;
import com.ak.util.Metrics;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class ResistanceThreeLayerTest {
  private ResistanceThreeLayerTest() {
  }

  @DataProvider(name = "layer-model")
  public static Object[][] threeLayerParameters() {
    return new Object[][] {
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {10, 0}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(5), new int[] {2, 1}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(10), new int[] {1, 5}, 10.0, 20.0, 309.342},

        {new double[] {8.0, 8.0, 8.0}, Metrics.fromMilli(10), new int[] {1, 5}, 10.0, 20.0,
            new ResistanceOneLayer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(8.0)
        },
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(10), new int[] {30, 30}, 10.0, 20.0,
            new ResistanceOneLayer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(8.0)
        },

        {new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0,
            new ResistanceTwoLayer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(8.0, 1.0, Metrics.fromMilli(5))},
        {new double[] {8.0, 2.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 242.751},
        {new double[] {8.0, 3.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 257.079},
        {new double[] {8.0, 4.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 269.694},
        {new double[] {8.0, 5.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 281.017},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0,
            new ResistanceTwoLayer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(8.0, 1.0, Metrics.fromMilli(10))},

        {new double[] {1.0, 1.0, 1.0}, Metrics.fromMilli(10), new int[] {1, 5}, 10.0, 20.0,
            new ResistanceOneLayer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(1.0)
        },

        {new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(0.01), new int[] {1, 1}, 10.0, 20.0,
            new ResistanceOneLayer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(1.0)
        },

        {new double[] {1.0, 1.0, 5.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 46.568},
        {new double[] {1.0, 1.0, 8.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 47.403},
        {new double[] {1.0, 5.0, 5.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 60.127},
        {new double[] {1.0, 5.0, 8.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 61.435},
        {new double[] {1.0, 8.0, 5.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 62.886},
        {new double[] {1.0, 8.0, 8.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 64.048},

        {new double[] {22.19, 1.57, 4.24}, Metrics.fromMilli(0.001), new int[] {154, 301}, 7.0, 7.0 * 5, 31.938},
        {new double[] {22.19, 1.57, 4.24}, Metrics.fromMilli(0.001), new int[] {154, 301}, 7.0, 7.0 * 3, 94.584},
        {new double[] {22.19, 1.57, 4.24}, Metrics.fromMilli(0.001), new int[] {154, 301}, 7.0 * 3, 7.0 * 5, 142.542},
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testLayer(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    Assert.assertEquals(new ResistanceTreeLayer(system, hStepSI).value(rho[0], rho[1], rho[2], p[0], p[1]), rOhm, 0.001);
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testNotClone() throws CloneNotSupportedException {
    new ResistanceTreeLayer(new TetrapolarSystem(1, 2, MILLI(METRE)), Metrics.fromMilli(0.001)).clone();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testInvalidFirstLayer() {
    new ResistanceTreeLayer(new TetrapolarSystem(1, 2, MILLI(METRE)), Metrics.fromMilli(0.001)).value(1.0, 2.0, 3.0, 0, 1);
  }

  @DataProvider(name = "experimental")
  public static Object[][] dhParameters() {
    return new Object[][] {
        {7.0, new double[] {34.58, 88.81, 141.1}, new double[] {34.58 - 0.03, 88.81 - 0.04, 141.1 - 0.06}},
    };
  }

  @Test(dataProvider = "experimental", enabled = false)
  public static void testInverseDh2(@Nonnegative double sPUmm, @Nonnull double[] rOhmBefore, @Nonnull double[] rOhmAfter) {
    TetrapolarSystem system0 = new TetrapolarSystem(sPUmm, sPUmm * 5.0, MILLI(METRE));
    TetrapolarSystem system1 = new TetrapolarSystem(sPUmm, sPUmm * 3.0, MILLI(METRE));
    TetrapolarSystem system2 = new TetrapolarSystem(sPUmm * 3.0, sPUmm * 5.0, MILLI(METRE));

    double rho1Apparent = system0.getApparent(rOhmBefore[0]);
    double rho2Apparent = system1.getApparent(rOhmBefore[1]);
    double rho3Apparent = system2.getApparent(rOhmBefore[2]);
    Logger.getAnonymousLogger().info(String.format("Apparent : %.3f; %.3f; %.3f", rho1Apparent, rho2Apparent, rho3Apparent));

    ResistanceTreeLayer[] predicted = Stream.of(system0, system1, system2).
        map(system -> new ResistanceTreeLayer(system, Metrics.fromMilli(0.001))).toArray(ResistanceTreeLayer[]::new);

    SimpleBounds bounds = new SimpleBounds(
        new double[] {0.0, 0.0, 0.0, 101.0, 1.0},
        new double[] {50.0, 50.0, 50.0, 10_000, 10_000}
    );

    PointValuePair pointValuePair = SimplexTest.optimizeCMAES(point -> {
      double rho1 = point[0];
      double rho2 = point[1];
      double rho3 = point[2];
      int p1 = (int) Math.round(point[3]);
      int p2 = (int) Math.round(point[4]);

      Inequality inequality = Inequality.expAndLogDifference();
      for (int i = 0; i < predicted.length; i++) {
        inequality.applyAsDouble(rOhmBefore[i], predicted[i].value(rho1, rho2, rho3, p1, p2));
        inequality.applyAsDouble(rOhmBefore[i] - rOhmAfter[i],
            predicted[i].value(rho1, rho2, rho3, p1, p2) - predicted[i].value(rho1, rho2, rho3, p1 - 100, p2));
      }
      return inequality.getAsDouble();
    }, bounds);
    Logger.getAnonymousLogger().info(Arrays.toString(pointValuePair.getPoint()));
  }
}