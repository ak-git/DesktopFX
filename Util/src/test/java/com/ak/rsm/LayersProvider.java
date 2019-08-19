package com.ak.rsm;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.annotations.DataProvider;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

class LayersProvider {
  private LayersProvider() {
  }

  /**
   * Generates optimal electrode system pair.
   * 10 x 30, 50 x 30 mm,
   * 7 x 21, 21 x 35 mm.
   *
   * @param smm small potential electrode distance, mm.
   * @return two Tetrapolar System.
   */
  @Nonnull
  private static TetrapolarSystem[] systems2(@Nonnegative double smm) {
    return new TetrapolarSystem[] {
        new TetrapolarSystem(smm, smm * 3.0, MILLI(METRE)),
        new TetrapolarSystem(smm * 5.0, smm * 3.0, MILLI(METRE)),
    };
  }

  /**
   * Generates optimal electrode system pair.
   * 10 x 30, 30 x 50, 10 x 50 mm,
   * 7 x 21, 21 x 35, 7 x 35 mm.
   *
   * @param smm small potential electrode distance, mm.
   * @return three Tetrapolar System.
   */
  @Nonnull
  private static TetrapolarSystem[] systems3(@Nonnegative double smm) {
    return new TetrapolarSystem[] {
        new TetrapolarSystem(smm, smm * 3.0, MILLI(METRE)),
        new TetrapolarSystem(smm * 5.0, smm * 3.0, MILLI(METRE)),
        new TetrapolarSystem(smm, smm * 5.0, MILLI(METRE)),
    };
  }

  /**
   * Generates optimal electrode system pair.
   * 10 x 30, 30 x 50, 10 x 50, 20 x 40, 40 x 60 mm,
   * 7 x 21, 21 x 35, 7 x 35, 14 x 28, 28 x 42 mm.
   *
   * @param smm small potential electrode distance, mm.
   * @return three Tetrapolar System.
   */
  @Nonnull
  private static TetrapolarSystem[] systems5(@Nonnegative double smm) {
    return new TetrapolarSystem[] {
        new TetrapolarSystem(smm, smm * 3.0, MILLI(METRE)),
        new TetrapolarSystem(smm * 3.0, smm * 5.0, MILLI(METRE)),
        new TetrapolarSystem(smm, smm * 5.0, MILLI(METRE)),
        new TetrapolarSystem(smm * 2, smm * 4.0, MILLI(METRE)),
        new TetrapolarSystem(smm * 4, smm * 6.0, MILLI(METRE)),
    };
  }

  @Nonnull
  private static ToDoubleFunction<TetrapolarSystem> layer1(@Nonnegative double rho) {
    return system -> new Resistance1Layer(system).value(rho);
  }

  @Nonnull
  private static ToDoubleFunction<TetrapolarSystem> layer2(@Nonnegative double rh1, @Nonnegative double rho2, @Nonnegative double hmm) {
    return system -> new Resistance2Layer(system).value(rh1, rho2, Metrics.fromMilli(hmm));
  }

  @Nonnull
  private static double[] rOhms(@Nonnull TetrapolarSystem[] systems, @Nonnull ToDoubleFunction<TetrapolarSystem> generator) {
    return Arrays.stream(systems).mapToDouble(generator).toArray();
  }

  @DataProvider(name = "theoryStaticParameters")
  public static Object[][] theoryStaticParameters() {
    TetrapolarSystem[] systems3 = systems3(10.0);
    return new Object[][] {
        {
            systems3,
            rOhms(systems3, layer1(1.0)),
        },
        {
            systems3,
            rOhms(systems3, layer1(2.0)),
        },
        {
            systems3,
            rOhms(systems3, layer2(9.0, 9.0, 10.0)),
        },

        {
            systems3,
            rOhms(systems3, layer2(9.0, 1.0, 10.0)),
        },
        {
            systems3,
            rOhms(systems3, layer2(1.0, 4.0, 2.0)),
        },
        {
            systems3,
            rOhms(systems3, layer2(0.7, Double.POSITIVE_INFINITY, 11.0)),
        },
    };
  }

  @DataProvider(name = "staticParameters")
  public static Object[][] staticParameters() {
    return new Object[][] {
        {
            systems3(7.0),
            new double[] {88.81, 141.1, 34.58},
        },
        {
            systems5(7.0),
            new double[] {123.3, 176.1, 43.09, 170.14, 85.84 * 2}
        },
        //vk
        {
            systems5(7.0),
            new double[] {96.7, 155.0, 36.56, 134.7, 79.9 * 2}
        },
    };
  }

  @DataProvider(name = "theoryDynamicParameters")
  public static Object[][] theoryDynamicParameters() {
    TetrapolarSystem[] systems2 = systems2(10);
    double dh = -0.1;
    return new Object[][] {
        {
            systems2,
            rOhms(systems2, layer1(1.0)),
            rOhms(systems2, layer1(1.0)),
            Metrics.fromMilli(dh)
        },
        {
            systems2,
            rOhms(systems2, layer1(2.0)),
            rOhms(systems2, layer1(2.0)),
            Metrics.fromMilli(dh)
        },
        {
            systems2,
            rOhms(systems2, layer2(9.0, 9.0, 10.0)),
            rOhms(systems2, layer2(9.0, 9.0, 10.0 + dh)),
            Metrics.fromMilli(dh)
        },

        {
            systems2,
            rOhms(systems2, layer2(9.0, 1.0, 10.0)),
            rOhms(systems2, layer2(9.0, 1.0, 10.0 + dh)),
            Metrics.fromMilli(dh)
        },
        {
            systems2,
            rOhms(systems2, layer2(1.0, 4.0, 2.0)),
            rOhms(systems2, layer2(1.0, 4.0, 2.0 + dh)),
            Metrics.fromMilli(dh)
        },
        {
            systems2,
            rOhms(systems2, layer2(0.7, Double.POSITIVE_INFINITY, 11.0)),
            rOhms(systems2, layer2(0.7, Double.POSITIVE_INFINITY, 11.0 + dh)),
            Metrics.fromMilli(dh)
        },
    };
  }

  @DataProvider(name = "theoryDynamicParameters3")
  public static Object[][] theoryDynamicParameters3() {
    TetrapolarSystem[] systems3 = systems3(10);
    double dh = -0.1;
    return new Object[][] {
        {
            systems3,
            rOhms(systems3, layer1(1.0)),
            rOhms(systems3, layer1(1.0)),
            Metrics.fromMilli(dh)
        },
        {
            systems3,
            rOhms(systems3, layer1(2.0)),
            rOhms(systems3, layer1(2.0)),
            Metrics.fromMilli(dh)
        },
        {
            systems3,
            rOhms(systems3, layer2(9.0, 9.0, 10.0)),
            rOhms(systems3, layer2(9.0, 9.0, 10.0 + dh)),
            Metrics.fromMilli(dh)
        },

        {
            systems3,
            rOhms(systems3, layer2(9.0, 1.0, 10.0)),
            rOhms(systems3, layer2(9.0, 1.0, 10.0 + dh)),
            Metrics.fromMilli(dh)
        },
        {
            systems3,
            rOhms(systems3, layer2(1.0, 4.0, 2.0)),
            rOhms(systems3, layer2(1.0, 4.0, 2.0 + dh)),
            Metrics.fromMilli(dh)
        },
        {
            systems3,
            rOhms(systems3, layer2(0.7, Double.POSITIVE_INFINITY, 11.0)),
            rOhms(systems3, layer2(0.7, Double.POSITIVE_INFINITY, 11.0 + dh)),
            Metrics.fromMilli(dh)
        },
    };
  }


  @DataProvider(name = "waterDynamicParameters2")
  public static Object[][] waterDynamicParameters2() {
    double dh = -Metrics.fromMilli(10.0 / 200.0);
    return new Object[][] {
        // h = 5 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(10.0),
            new double[] {30.971, 61.860},
            new double[] {31.278, 62.479},
            dh
        },
        // h = 10 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(10.0),
            new double[] {16.761, 32.246},
            new double[] {16.821, 32.383},
            dh
        },
        // h = 15 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(10.0),
            new double[] {13.338, 23.903},
            new double[] {13.357, 23.953},
            dh
        },
        // h = 20 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(10.0),
            new double[] {12.187, 20.567},
            new double[] {12.194, 20.589},
            dh
        },
        // h = 25 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(10.0),
            new double[] {11.710, 18.986},
            new double[] {11.714, 18.998},
            dh
        },
        // h = 30 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(10.0),
            new double[] {11.482, 18.152},
            new double[] {11.484, 18.158},
            dh
        },
        // h = 35 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(10.0),
            new double[] {11.361, 17.674},
            new double[] {11.362, 17.678},
            dh
        },
    };
  }

  @DataProvider(name = "waterDynamicParameters3")
  public static Object[][] waterDynamicParameters3() {
    double dh = -Metrics.fromMilli(10.0 / 200.0);
    return new Object[][] {
        // h = 5 mm, rho1 = 0.7, rho2 = Inf
        {
            systems3(10.0),
            new double[] {30.971, 61.860, 18.069},
            new double[] {31.278, 62.479, 18.252},
            dh
        },
        // h = 10 mm, rho1 = 0.7, rho2 = Inf
        {
            systems3(10.0),
            new double[] {16.761, 32.246, 9.074},
            new double[] {16.821, 32.383, 9.118},
            dh
        },
        // h = 15 mm, rho1 = 0.7, rho2 = Inf
        {
            systems3(10.0),
            new double[] {13.338, 23.903, 6.267},
            new double[] {13.357, 23.953, 6.284},
            dh
        },
        // h = 20 mm, rho1 = 0.7, rho2 = Inf
        {
            systems3(10.0),
            new double[] {12.187, 20.567, 5.082},
            new double[] {12.194, 20.589, 5.090},
            dh
        },
        // h = 25 mm, rho1 = 0.7, rho2 = Inf
        {
            systems3(10.0),
            new double[] {11.710, 18.986, 4.514},
            new double[] {11.714, 18.998, 4.518},
            dh
        },
        // h = 30 mm, rho1 = 0.7, rho2 = Inf
        {
            systems3(10.0),
            new double[] {11.482, 18.152, 4.216},
            new double[] {11.484, 18.158, 4.218},
            dh
        },
        // h = 35 mm, rho1 = 0.7, rho2 = Inf
        {
            systems3(10.0),
            new double[] {11.361, 17.674, 4.047},
            new double[] {11.362, 17.678, 4.048},
            dh
        },
    };
  }

  @DataProvider(name = "dynamicParameters3")
  public static Object[][] dynamicParameters3() {
    return new Object[][] {
        {
            systems3(7.0),
            new double[] {88.81, 141.1, 34.58},
            new double[] {88.81 - 0.04, 141.1 - 0.06, 34.58 - 0.03},
            -Metrics.fromMilli(0.1)
        },
        {
            systems3(7.0),
            new double[] {123.3, 176.1, 43.09},
            new double[] {123.3 - 0.1, 176.1 - 0.125, 43.09 - 0.04},
            -Metrics.fromMilli(0.1)
        },
        {
            systems5(7.0),
            new double[] {123.3, 176.1, 43.09, 170.14, 85.84 * 2},
            new double[] {123.3 - 0.1, 176.1 - 0.125, 43.09 - 0.04, 170.14 - 0.16, 85.84 * 2 - 0.1 * 2},
            -Metrics.fromMilli(0.1)
        },
    };
  }
}
