package com.ak.rsm;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

        {new double[] {10.0, 5.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 99.949},// briko: 101.99
        {new double[] {5.0, 10.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 103.657},// briko: 104.87
        {new double[] {1.0, 5.0, 10.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 49.651},// briko: 53.11
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

  @Test(dataProviderClass = LayersProvider.class, dataProvider = "staticParameters5", invocationCount = 20, enabled = false)
  public static void testInverseStatic(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms) {
    ResistanceTreeLayer[] predicted = Stream.of(systems).map(system -> new ResistanceTreeLayer(system, Metrics.fromMilli(0.1))).toArray(ResistanceTreeLayer[]::new);
    double[] apparent = IntStream.range(0, systems.length).mapToDouble(i -> systems[i].getApparent(rOhms[i])).toArray();
    Logger.getAnonymousLogger().config(Arrays.toString(apparent));

    SimpleBounds bounds = new SimpleBounds(
        new double[] {apparent[2], 0.7, 0.7, 2, 1},
        new double[] {100.0, apparent[2], 100.0, 10_0, 10_0}
    );

    PointValuePair pair = SimplexTest.optimizeCMAES(x -> {
          int p1 = (int) Math.max(Math.round(x[3]), 1);
          int p2 = (int) Math.max(Math.round(x[4]), 2);
          return Inequality.proportional().applyAsDouble(rOhms, i -> predicted[i].value(x[0], x[1], x[2], p1, p2));
        },
        bounds, new double[] {apparent[2], apparent[2], apparent[2], 2, 1}, new double[] {0.01, 0.01, 0.01, 1, 1});
    Logger.getAnonymousLogger().info(Arrays.toString(pair.getPoint()) + " : " + pair.getValue());
  }

  @Test(dataProviderClass = LayersProvider.class, dataProvider = "theoryDynamicParameters3", enabled = false)
  public static void testInverseDynamic(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
    ResistanceTreeLayer[] predicted = Stream.of(systems).map(system -> new ResistanceTreeLayer(system, Math.abs(dh))).toArray(ResistanceTreeLayer[]::new);
    DoubleSummaryStatistics apparent = IntStream.range(0, systems.length).mapToDouble(i -> systems[i].getApparent(rOhmsBefore[i])).summaryStatistics();

    BiFunction<int[], double[], PointValuePair> rhos = (hs, rhoInit) -> SimplexTest.optimizeNelderMead(rho -> {
      for (double v : rho) {
        if (v < 0.1) {
          return Double.POSITIVE_INFINITY;
        }
      }
      return Inequality.proportional().applyAsDouble(rOhmsBefore, i -> predicted[i].value(rho[0], rho[1], rho[2], hs[0], hs[1]));
    }, rhoInit, new double[] {1.0, 1.0, 1.0});

    double[] rhoInit = {apparent.getAverage(), apparent.getAverage(), apparent.getAverage()};
    PointValuePair hs = SimplexTest.optimizeNelderMead(p -> {
      p[0] = (int) Math.max(Math.round(p[0]), 2);
      p[1] = (int) Math.max(Math.round(p[1]), 2);
      int p1 = (int) p[0];
      int p2 = (int) p[1];
      double[] dHMeasured = new double[rOhmsBefore.length];
      Arrays.setAll(dHMeasured, i -> (systems[i].getL() / rOhmsBefore[i]) * (rOhmsAfter[i] - rOhmsBefore[i]) / dh);

      PointValuePair pair = rhos.apply(new int[] {p1, p2}, rhoInit);
      double rho1 = pair.getPoint()[0];
      double rho2 = pair.getPoint()[1];
      double rho3 = pair.getPoint()[2];
      for (int i = 0; i < rhoInit.length; i++) {
        rhoInit[i] = pair.getPoint()[i];
      }

      Inequality inequality = Inequality.absolute();
      inequality.applyAsDouble(dHMeasured, i -> (systems[i].getL() / predicted[i].value(rho1, rho2, rho3, p1, p2)) *
          ((predicted[i].value(rho1, rho2, rho3, p1 - 1, p2) - predicted[i].value(rho1, rho2, rho3, p1, p2)) / dh));

      Logger.getAnonymousLogger().config(String.format("%d %d %s %.6g", p1, p2, Arrays.toString(pair.getPoint()), inequality.getAsDouble()));

      return inequality.getAsDouble();
    }, new double[] {1000, 1000}, new double[] {100, 100});
    Logger.getAnonymousLogger().info(Arrays.stream(hs.getPoint()).map(operand -> Math.abs(operand * dh * 1000)).mapToObj(value -> String.format("%.2f", value)).collect(Collectors.joining(", ")));
  }
}