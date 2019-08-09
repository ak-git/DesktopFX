package com.ak.rsm;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.util.LineFileBuilder;
import com.ak.util.Metrics;
import com.ak.util.Strings;
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
        {new double[] {22.19, 1.57, 4.24}, Metrics.fromMilli(0.001), new int[] {154, 301}, 7.0 * 3, 7.0 * 5, 142.539},

        {new double[] {10.0, 5.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 99.949},// briko: 101.99
        {new double[] {5.0, 10.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 103.657},// briko: 104.87
        {new double[] {1.0, 5.0, 10.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 49.651},// briko: 53.11

        {new double[] {10.0, 1.0, 10.0}, Metrics.fromMilli(0.1), new int[] {1, 1}, 10.0, 30.0, 156.160},
        {new double[] {10.0, 1.0, 10.0}, Metrics.fromMilli(0.001), new int[] {3_000, 100}, 10.0, 30.0, 149.637},
        {new double[] {10.0, 1.0, 10.0}, Metrics.fromMilli(0.1), new int[] {1000, 1}, 10.0, 30.0, 159.154},
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testLayer(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
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
  public static void testLayerSpecial(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                                      @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    Assert.assertEquals(new Resistance3Layer(system, hStepSI).value(rho[0], rho[1], rho[2], p[0], p[1]), rOhm, 0.001, Arrays.toString(rho));
  }

  @Test(enabled = false)
  public static void testContinuous() throws IOException {
    TetrapolarSystem system = new TetrapolarSystem(10.0, 30.0, MILLI(METRE));
    LineFileBuilder.of("%.1f %.0f %.3f")
        .xRange(0.1, 50.0, 0.1)
        .yRange(0, 2, 1)
        .generate("z.txt", (h1mm, index) -> {
          int i = (int) index;
          if (i == 0) {
            int p1 = (int) (h1mm / 0.1);
            return new Resistance3Layer(system, Metrics.fromMilli(0.1)).value(10.0, 1.0, 10.0, p1, 1);
          }
          else if (i == 1) {
            return new Resistance2Layer(system).value(10.0, 1.0, Metrics.fromMilli(h1mm));
          }
          else if (i == 2) {
            return new Resistance2Layer(system).value(1.0, 10.0, Metrics.fromMilli(h1mm));
          }
          else {
            return Double.NaN;
          }
        });
  }

  private static Object[] generate(@Nonnull int[] mm, @Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p) {
    Logger.getAnonymousLogger().log(Level.INFO,
        String.format("%s = %.2f; %s = %.2f",
            Strings.K_12, Layers.getK12(rho[0], rho[1]),
            Strings.K_23, Layers.getK12(rho[1], rho[2])
        )
    );
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

  @DataProvider(name = "dynamicParameters")
  public static Object[][] waterDynamicParameters3() {
    return new Object[][] {
        generate(new int[] {10, 30, 50}, new double[] {9.0, 1.0, 4.0}, Metrics.fromMilli(0.1), new int[] {5, 5}),
        {
            new TetrapolarSystem[][] {
                new TetrapolarSystem[] {
                    new TetrapolarSystem(7.0, 21.0, MILLI(METRE)),
                    new TetrapolarSystem(35.0, 21.0, MILLI(METRE)),
                },
                new TetrapolarSystem[] {
                    new TetrapolarSystem(21.0, 35.0, MILLI(METRE)),
                    new TetrapolarSystem(7.0, 35.0, MILLI(METRE)),
                }
            },
            new double[] {88.81 - 0.04, 141.1 - 0.06, 141.1 - 0.06, 34.58 - 0.03},
            new double[] {88.81, 141.1, 141.1, 34.58},
            Metrics.fromMilli(0.1)
        },
        {
            new TetrapolarSystem[][] {
                new TetrapolarSystem[] {
                    new TetrapolarSystem(7.0, 21.0, MILLI(METRE)),
                    new TetrapolarSystem(21.0, 35.0, MILLI(METRE)),
                },
                new TetrapolarSystem[] {
                    new TetrapolarSystem(7.0, 35.0, MILLI(METRE)),
                    new TetrapolarSystem(14.0, 28.0, MILLI(METRE)),
                },
            },
            new double[] {123.3 - 0.1, 176.1 - 0.125, 43.09 - 0.04, 170.14 - 0.16},
            new double[] {123.3, 176.1, 43.09, 170.14},
            Metrics.fromMilli(0.1)
        }
    };
  }

  @Test(dataProvider = "dynamicParameters", enabled = false)
  public static void testInverseDynamic(@Nonnull TetrapolarSystem[][] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) throws IOException {
    Logger.getAnonymousLogger().log(Level.INFO, Resistance2Layer.Medium.inverse(systems[0], rOhmsBefore, rOhmsAfter, dh).toString());

    DoubleBinaryOperator subtract = (left, right) -> left - right;
    double[] subLogApparent = IntStream.range(0, systems.length).mapToDouble(j -> IntStream.range(0, systems[j].length)
        .mapToDouble(i -> log(new Resistance1Layer(systems[j][i]).getApparent(rOhmsBefore[j * 2 + i]))).reduce(subtract).orElseThrow()).toArray();
    double[] subLogDiff = IntStream.range(0, systems.length).mapToDouble(j -> IntStream.range(0, systems[j].length)
        .mapToDouble(i -> log(Math.abs((rOhmsAfter[j * 2 + i] - rOhmsBefore[j * 2 + i]) / dh))).reduce(subtract).orElseThrow()).toArray();

    int p1 = 5;
    int p2 = 5;
    LineFileBuilder.of("%.1f %.1f %.4f")
        .xRange(-1, 1, 0.1)
        .yRange(-1, 1, 0.1)
        .generate("z.txt", (k12, k23) -> {
          double[] subLogApparentPredicted = Arrays.stream(systems).mapToDouble(s -> Arrays.stream(s)
              .mapToDouble(system -> new Log1pApparent3Rho(system.sToL(), system.Lh(dh)).value(k12, k23, p1, p2))
              .reduce(subtract).orElseThrow()).toArray();

          double[] subLogDiffPredicted = Arrays.stream(systems).mapToDouble(s -> Arrays.stream(s)
              .mapToDouble(system -> {
                Resistance3Layer resistance3Layer = new Resistance3Layer(system, dh);
                double rho1 = 1.0;
                double rho2 = rho1 / Layers.getRho1ToRho2(k12);
                double rho3 = rho2 / Layers.getRho1ToRho2(k23);
                return log(Math.abs(
                    (resistance3Layer.value(rho1, rho2, rho3, p1 + 1, p2) -
                        resistance3Layer.value(rho1, rho2, rho3, p1, p2)) / dh
                    )
                );
              })
              .reduce(subtract).orElseThrow()).toArray();
          return Inequality.absolute().applyAsDouble(subLogApparent, subLogApparentPredicted);
        });
  }
}