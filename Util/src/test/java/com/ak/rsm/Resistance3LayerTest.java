package com.ak.rsm;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.SimplexTest;
import com.ak.util.Metrics;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.StrictMath.log;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class Resistance3LayerTest {
  private Resistance3LayerTest() {
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
            new Resistance1Layer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(8.0)
        },
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(10), new int[] {30, 30}, 10.0, 20.0,
            new Resistance1Layer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(8.0)
        },

        {new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0,
            new Resistance2Layer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(8.0, 1.0, Metrics.fromMilli(5))},
        {new double[] {8.0, 2.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 242.751},
        {new double[] {8.0, 3.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 257.079},
        {new double[] {8.0, 4.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 269.694},
        {new double[] {8.0, 5.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 281.017},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0,
            new Resistance2Layer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(8.0, 1.0, Metrics.fromMilli(10))},

        {new double[] {1.0, 1.0, 1.0}, Metrics.fromMilli(10), new int[] {1, 5}, 10.0, 20.0,
            new Resistance1Layer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(1.0)
        },

        {new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(0.01), new int[] {1, 1}, 10.0, 20.0,
            new Resistance1Layer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(1.0)
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
  public static void testLayer(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                               @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    Assert.assertEquals(new Resistance3Layer(system, hStepSI).value(rho[0], rho[1], rho[2], p[0], p[1]), rOhm, 0.001);
  }

  private static Object[] generate(@Nonnull int[] mm, @Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p) {
    TetrapolarSystem[][] tetrapolarSystems = {
        new TetrapolarSystem[] {
            new TetrapolarSystem(mm[0], mm[1], MILLI(METRE)),
            new TetrapolarSystem(mm[2], mm[1], MILLI(METRE)),
        },
        new TetrapolarSystem[] {
            new TetrapolarSystem(mm[1], mm[2], MILLI(METRE)),
            new TetrapolarSystem(mm[0], mm[2], MILLI(METRE)),
        },
    };

    double[] rOhmsBefore = Arrays.stream(tetrapolarSystems).flatMap(Arrays::stream)
        .mapToDouble(s -> new Resistance3Layer(s, hStepSI).value(rho[0], rho[1], rho[2], p[0], p[1])).toArray();
    double[] rOhmsAfter = Arrays.stream(tetrapolarSystems).flatMap(Arrays::stream)
        .mapToDouble(s -> new Resistance3Layer(s, hStepSI).value(rho[0], rho[1], rho[2], p[0] + 1, p[1])).toArray();
    return new Object[] {tetrapolarSystems, rOhmsBefore, rOhmsAfter, hStepSI};
  }

  @DataProvider(name = "waterDynamicParameters3")
  public static Object[][] waterDynamicParameters3() {
    return new Object[][] {
        generate(new int[] {10, 30, 50}, new double[] {1.0, 9.0, 1.0}, Metrics.fromMilli(0.1), new int[] {50, 50}),
    };
  }

  @Test(dataProvider = "waterDynamicParameters3", enabled = false)
  public static void testInverseDynamic3(@Nonnull TetrapolarSystem[][] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
    DoubleBinaryOperator subtract = (left, right) -> left - right;
    double[] subLogApparent = IntStream.range(0, systems.length).mapToDouble(j -> IntStream.range(0, systems[j].length)
        .mapToDouble(i -> log(new Resistance1Layer(systems[j][i]).getApparent(rOhmsBefore[j * 2 + i]))).reduce(subtract).orElseThrow()).toArray();
    double[] subLogDiff = IntStream.range(0, systems.length).mapToDouble(j -> IntStream.range(0, systems[j].length)
        .mapToDouble(i -> log(Math.abs((rOhmsAfter[j * 2 + i] - rOhmsBefore[j * 2 + i]) / dh))).reduce(subtract).orElseThrow()).toArray();

    double h = Metrics.fromMilli(0.1);
    MultivariateFunction multivariateFunction = p -> {
      double k12 = Math.min(Math.max(p[0], -1), 1);
      double k23 = Math.min(Math.max(p[1], -1), 1);
      int p1 = (int) Math.abs(Math.round(p[2]));
      int p2mp1 = (int) Math.abs(Math.round(p[3]));

      double[] subLogApparentPredicted = Arrays.stream(systems).mapToDouble(s -> Arrays.stream(s)
          .mapToDouble(system -> new Log1pApparent3Rho(system.sToL(), system.Lh(h)).value(k12, k23, p1, p2mp1)).reduce(subtract).orElseThrow()).toArray();

      double[] subLogDiffPredicted = Arrays.stream(systems).mapToDouble(s -> Arrays.stream(s)
          .mapToDouble(system -> new LogDerivativeApparent3Rho(system.sToL(), system.Lh(h)).value(k12, k23, p1, p2mp1)).reduce(subtract).orElseThrow()).toArray();

      Inequality inequality = Inequality.absolute();
      inequality.applyAsDouble(subLogApparent, i -> subLogApparentPredicted[i]);
      inequality.applyAsDouble(subLogDiff, i -> subLogDiffPredicted[i]);
      return inequality.getAsDouble();
    };

    PointValuePair p = SimplexTest.optimizeCMAES(multivariateFunction,
        new SimpleBounds(new double[] {-1, -1, 10, 10}, new double[] {1, 1, 100, 100}),
        new double[] {0.1, 0.1, 1, 1});
    Logger.getAnonymousLogger().info(Arrays.toString(p.getPoint()));
  }
}