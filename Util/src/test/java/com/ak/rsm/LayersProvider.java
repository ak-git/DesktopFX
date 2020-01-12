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
   *
   * @return two Tetrapolar System.
   */
  @Nonnull
  static TetrapolarSystem[] systems2_10mm() {
    double smm = 10.0;
    return new TetrapolarSystem[] {
        new TetrapolarSystem(smm, smm * 3.0, MILLI(METRE)),
        new TetrapolarSystem(smm * 5.0, smm * 3.0, MILLI(METRE)),
    };
  }

  /**
   * Generates optimal electrode system pair.
   * 10 x 30, 30 x 50, 20 x 40, 40 x 60 mm,
   * 7 x 21, 21 x 35, 14 x 28, 28 x 42 mm.
   *
   * @param smm small potential electrode distance, mm.
   * @return three Tetrapolar System.
   */
  @Nonnull
  static TetrapolarSystem[] systems4(@Nonnegative double smm) {
    return new TetrapolarSystem[] {
        new TetrapolarSystem(smm, smm * 3.0, MILLI(METRE)),
        new TetrapolarSystem(smm * 3.0, smm * 5.0, MILLI(METRE)),
        new TetrapolarSystem(smm * 2.0, smm * 4.0, MILLI(METRE)),
        new TetrapolarSystem(smm * 4.0, smm * 6.0, MILLI(METRE)),
    };
  }

  /**
   * Generates optimal electrode system pair.
   * 7 x 21, 21 x 35, 7 x 35, 14 x 28, 28 x 42 mm.
   *
   * @return three Tetrapolar System.
   */
  @Nonnull
  private static TetrapolarSystem[] systems5_7mm() {
    double smm = 7.0;
    return new TetrapolarSystem[] {
        new TetrapolarSystem(smm, smm * 3.0, MILLI(METRE)),
        new TetrapolarSystem(smm * 3.0, smm * 5.0, MILLI(METRE)),
        new TetrapolarSystem(smm, smm * 5.0, MILLI(METRE)),
        new TetrapolarSystem(smm * 2, smm * 4.0, MILLI(METRE)),
        new TetrapolarSystem(smm * 4, smm * 6.0, MILLI(METRE)),
    };
  }

  @Nonnull
  static ToDoubleFunction<TetrapolarSystem> layer1(@Nonnegative double rho) {
    return system -> new Resistance1Layer(system).value(rho);
  }

  @Nonnull
  static ToDoubleFunction<TetrapolarSystem> layer2(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double hmm) {
    return system -> new Resistance2Layer(system).value(rho1, rho2, Metrics.fromMilli(hmm));
  }

  @Nonnull
  static ToDoubleFunction<TetrapolarSystem> layer3(@Nonnull double[] rho, double hmmStep, @Nonnegative int p1, @Nonnegative int p2mp1) {
    return system -> new Resistance3Layer(system, Math.abs(Metrics.fromMilli(hmmStep))).value(rho[0], rho[1], rho[2], p1, p2mp1);
  }

  @Nonnull
  static double[] rOhms(@Nonnull TetrapolarSystem[] systems, @Nonnull ToDoubleFunction<TetrapolarSystem> generator) {
    return Arrays.stream(systems).mapToDouble(generator).toArray();
  }

  @DataProvider(name = "dynamicParameters3")
  public static Object[][] dynamicParameters3() {
    return new Object[][] {
        {
            systems5_7mm(),
            new double[] {123.3, 176.1, 43.09, 170.14, 85.84 * 2},
            new double[] {123.3 - 0.1, 176.1 - 0.125, 43.09 - 0.04, 170.14 - 0.16, 85.84 * 2 - 0.1 * 2},
            Metrics.fromMilli(-0.1)
        },

        {
            systems4(7.0),
            new double[] {113.575, 167.775, 74.49 * 2, 167.50 * 2 - 74.49 * 2},
            new double[] {113.405, 167.400, 74.36 * 2, 167.15 * 2 - 74.36 * 2},
            Metrics.fromMilli(-0.15)
        },
        {
            systems4(7.0),
            new double[] {110.44, 161.15, 72.49 * 2, 162.15 * 2 - 72.49 * 2},
            new double[] {110.33, 160.70, 72.36 * 2, 161.70 * 2 - 72.36 * 2},
            Metrics.fromMilli(-0.15)
        },
    };
  }

  @DataProvider(name = "waterDynamicParameters2E6275")
  public static Object[][] waterDynamicParameters2E6275() {
    double dh = -Metrics.fromMilli(10.0 / 200.0);
    return new Object[][] {
        // h = 5 mm, rho1 = 0.7, rho2 = Inf
        {
            systems4(10.0),
            new double[] {29.80, 65.775, 50.0, 40.325 * 2},
            new double[] {30.10, 66.425, 50.55, 40.75 * 2},
            dh
        },
    };
  }
}
