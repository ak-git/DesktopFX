package com.ak.rsm;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.LineFileBuilder;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.rsm.LayersProvider.layer3;
import static com.ak.rsm.LayersProvider.rOhms;
import static com.ak.rsm.LayersProvider.systems4;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class Resistance3LayerTest {
  @DataProvider(name = "layer-model")
  public static Object[][] threeLayerParameters() {
    return new Object[][] {
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {0, 10}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {10, 0}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {10, 0}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(5), new int[] {2, 1}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(10), new int[] {1, 5}, 10.0, 20.0, 309.342},

        {new double[] {8.0, 8.0, 8.0}, Metrics.fromMilli(10), new int[] {1, 5}, 10.0, 20.0,
            new Resistance1Layer(new TetrapolarSystem(10.0, 20.0, MILLI(METRE))).value(8.0)
        },
        {new double[] {8.0, 8.0, 8.0}, Metrics.fromMilli(10), new int[] {0, 0}, 10.0, 20.0,
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
        {new double[] {22.19, 1.57, 4.24}, Metrics.fromMilli(0.001), new int[] {154, 301}, 7.0 * 3, 7.0 * 5, 142.539},

        {new double[] {10.0, 5.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 99.949},// briko: 101.99
        {new double[] {5.0, 10.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 103.657},// briko: 104.87
        {new double[] {1.0, 5.0, 10.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 49.651},// briko: 53.11

        {new double[] {10.0, 1.0, 10.0}, Metrics.fromMilli(0.1), new int[] {3_0, 1}, 10.0, 30.0, 149.637},
        {new double[] {10.0, 1.0, 10.0}, Metrics.fromMilli(0.01), new int[] {3_00, 10}, 10.0, 30.0, 149.637},
        {new double[] {10.0, 1.0, 10.0}, Metrics.fromMilli(0.1), new int[] {1000, 1}, 10.0, 30.0, 159.154},
    };
  }

  @Test(dataProvider = "layer-model")
  public void testLayer(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                        @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    Assert.assertEquals(new Resistance3Layer(system, hStepSI).value(rho[0], rho[1], rho[2], p[0], p[1]), rOhm, 0.001, Arrays.toString(rho));
  }

  @DataProvider(name = "layer-model-special")
  public static Object[][] threeLayerParametersSpecial() {
    return new Object[][] {
        {new double[] {10.0, 1.0, 1.0}, Metrics.fromMilli(0.1), new int[] {10, 0}, 10.0, 30.0,
            new Resistance2Layer(new TetrapolarSystem(10.0, 30.0, MILLI(METRE))).value(10.0, 1.0, Metrics.fromMilli(1))},
        {new double[] {1.0, 10.0, 1.0}, Metrics.fromMilli(0.1), new int[] {0, 10}, 10.0, 30.0,
            new Resistance2Layer(new TetrapolarSystem(10.0, 30.0, MILLI(METRE))).value(10.0, 1.0, Metrics.fromMilli(1))},
        {new double[] {1.0, 1.0, 10.0}, Metrics.fromMilli(0.1), new int[] {0, 0}, 10.0, 30.0,
            new Resistance1Layer(new TetrapolarSystem(10.0, 30.0, MILLI(METRE))).value(10.0)},
    };
  }

  @Test(dataProvider = "layer-model-special")
  public void testLayerSpecial(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                               @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    Assert.assertEquals(new Resistance3Layer(system, hStepSI).value(rho[0], rho[1], rho[2], p[0], p[1]), rOhm, 0.001, Arrays.toString(rho));
  }

  @Test(enabled = true)
  public void testContinuous() {
    for (int a : IntStream.rangeClosed(1, 30).toArray()) {
      TetrapolarSystem system = new TetrapolarSystem(a, a * 3, MILLI(METRE));
      for (double rho2 : new double[] {2.7 * 0.95, 2.7, 2.7 * 1.05}) {
        for (int rho1 : IntStream.iterate(10, x -> x < 70, x -> x + 10).toArray()) {
          LineFileBuilder.of("%.1f %.1f %.3f")
              .xRange(0, 20, 0.1)
              .yRange(0, 50, 0.1)
              .generate(String.format("rho1 = %d, rho2 = %.3f, a = %d mm.txt", rho1, rho2, a), (h1, h2) -> {
                int p1 = (int) h1 * 10;
                int p2mp1 = (int) h2 * 10;
                return new Resistance3Layer(system, Metrics.fromMilli(0.1)).value(rho1, rho2, 100.0, p1, p2mp1);
              });
        }
      }
    }
  }

  @DataProvider(name = "theoryDynamicParameters3")
  public static Object[][] theoryDynamicParameters3() {
    TetrapolarSystem[] systems4 = systems4(10);
    double hmm = 0.1;
    double dHmm = -0.3;
    return new Object[][] {
        {
            systems4,
            rOhms(systems4, layer3(new double[] {9.0, 1.0, 4.0}, hmm, 60, 30)),
            rOhms(systems4, layer3(new double[] {9.0, 1.0, 4.0}, hmm, 60 + (int) Math.round(dHmm / hmm), 30)),
            Metrics.fromMilli(dHmm),
            new double[] {Metrics.fromMilli(hmm) * 60, Metrics.fromMilli(hmm) * 30}
        },
    };
  }

  @Test(dataProvider = "theoryDynamicParameters3", enabled = false)
  @ParametersAreNonnullByDefault
  public void testInverse(TetrapolarSystem[] systems, double[] rOhmsBefore, double[] rOhmsAfter, double dH, double[] expectedH) {
    Random random = new Random();
    double[] noise = IntStream.range(0, systems.length).mapToDouble(value -> random.nextGaussian() / 10).toArray();
    for (int i = 0; i < noise.length; i++) {
      rOhmsBefore[i] += noise[i];
      rOhmsAfter[i] += noise[i];
    }
    Medium medium = Resistance3Layer.inverseDynamic(systems, rOhmsBefore, rOhmsAfter, dH);
    Logger.getLogger(Resistance3Layer.class.getName()).warning(() -> String.format("3 Layers - inverseDynamic%n%s", medium));
    Assert.assertEquals(medium.getH(), expectedH, Metrics.fromMilli(2));
  }

  @DataProvider(name = "akDynamicParameters3")
  public static Object[][] akDynamicParameters3() {
    double dHmm = -0.14;
    return new Object[][] {
        {
            systems4(6.0),
            new double[] {134.90, 172.80, 190.60, 154.80},
            new double[] {134.88, 172.72, 190.50, 154.75},
            Metrics.fromMilli(dHmm)
        },
        {
            systems4(7.0),
            new double[] {113.56, 167.80, 148.925, 185.90},
            new double[] {113.43, 167.50, 148.725, 185.60},
            Metrics.fromMilli(dHmm)
        },
        {
            systems4(8.0),
            new double[] {110.36, 165.05, 147.94, 180.05},
            new double[] {110.30, 164.90, 147.82, 179.85},
            Metrics.fromMilli(dHmm)
        },
        {
            systems4(7.0),
            new double[] {125.74, 168.20, 153.60, 185.20},
            new double[] {125.77, 168.00, 153.70, 185.10},
            Metrics.fromMilli(dHmm)
        },
        {
            systems4(7.0),
            new double[] {127.42, 171.90, 156.42, 190.00},
            new double[] {127.37, 171.75, 156.30, 189.80},
            Metrics.fromMilli(dHmm)
        },
    };
  }

  @Test(dataProvider = "akDynamicParameters3", enabled = false)
  @ParametersAreNonnullByDefault
  public void testInverse2(TetrapolarSystem[] systems, double[] rOhmsBefore, double[] rOhmsAfter, double dH) {
    Logger logger = Logger.getLogger(Resistance3LayerTest.class.getName());
    logger.config(() -> String.format("2 Layers - inverseStaticLinear%n%s", Resistance2Layer.inverseStaticLinear(systems, rOhmsBefore)));
    logger.config(() -> String.format("2 Layers - inverseStaticLog%n%s", Resistance2Layer.inverseStaticLog(systems, rOhmsBefore)));
    logger.info(() -> String.format("2 Layers - inverseDynamic%n%s%n", Resistance2Layer.inverseDynamic(systems, rOhmsBefore, rOhmsAfter, dH)));
  }

  @Test(dataProvider = "akDynamicParameters3", enabled = false)
  @ParametersAreNonnullByDefault
  public void testInverse3(TetrapolarSystem[] systems, double[] rOhmsBefore, double[] rOhmsAfter, double dH) {
    Logger.getLogger(Resistance3LayerTest.class.getName()).warning(
        () -> String.format("3 Layers - inverseDynamic %s %n%s%n",
            Arrays.toString(systems),
            Resistance3Layer.inverseDynamic(systems, rOhmsBefore, rOhmsAfter, dH))
    );
  }
}