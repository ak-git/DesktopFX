package com.ak.rsm;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.annotations.DataProvider;

import static com.ak.rsm.TetrapolarSystem.systems4;

enum LayersProvider {
  ;

  @Nonnull
  static ToDoubleFunction<TetrapolarSystem> layer1(@Nonnegative double rho) {
    return system -> new Resistance1Layer(system).value(rho);
  }

  @Nonnull
  static ToDoubleFunction<TetrapolarSystem> layer2(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
    return system -> new Resistance2Layer(system).value(rho1, rho2, h);
  }

  @Nonnull
  static ToDoubleFunction<TetrapolarSystem> layer3(@Nonnull double[] rho, double hmmStep, @Nonnegative int p1, @Nonnegative int p2mp1) {
    return system -> new Resistance3Layer(system, Math.abs(Metrics.fromMilli(hmmStep))).value(rho[0], rho[1], rho[2], p1, p2mp1);
  }

  @Nonnull
  static double[] rangeSystems(@Nonnull TetrapolarSystem[] systems, @Nonnull ToDoubleFunction<TetrapolarSystem> generator) {
    return Arrays.stream(systems).mapToDouble(generator).toArray();
  }

  @DataProvider(name = "dynamicParameters3")
  public static Object[][] dynamicParameters3() {
    return new Object[][] {
        {
            systems4(7.0),
            new double[] {113.575, 167.775, 149.0, 186.0},
            new double[] {113.575 - 0.170 / 3, 167.775 - 0.375 / 3, 149.0 - 0.3 / 3, 186.0 - 0.4 / 3},
            Metrics.fromMilli(-0.15 / 3)
        },
        {
            systems4(7.0),
            new double[] {110.44, 161.15, 145.00, 179.00},
            new double[] {110.44 - 0.11 / 2, 161.15 - 0.45 / 2, 145.0 - 0.3 / 2, 179.00 - 0.5 / 2},
            Metrics.fromMilli(-0.15 / 2)
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
