package com.ak.rsm.resistance;

import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Resistance2LayerTest {
  static Stream<Arguments> twoLayerParameters() {
    return Stream.of(
        arguments(new double[] {8.0, 1.0}, 10.0, 10.0, 20.0, 309.342),
        arguments(new double[] {8.0, 1.0}, 10.0, 30.0, 90.0, 8.815),
        arguments(new double[] {8.0, 1.0}, 50.0, 10.0, 20.0, 339.173),
        arguments(new double[] {8.0, 1.0}, 50.0, 30.0, 90.0, 38.858),

        arguments(new double[] {1.0, 1.0}, 0.0, 20.0, 40.0, 21.221),
        arguments(new double[] {2.0, 2.0}, 0.0, 20.0, 40.0, 21.221 * 2.0),
        arguments(new double[] {1.0, 1.0}, 0.0, 40.0, 80.0, 10.610),
        arguments(new double[] {0.5, 0.5}, 0.0, 40.0, 80.0, 10.610 / 2.0),

        arguments(new double[] {3.5, 1.35}, 10.0, 20.0, 40.0, 59.108),
        arguments(new double[] {5.0, 2.0}, 15.0, 20.0, 40.0, 95.908),
        arguments(new double[] {7.0, 1.0}, 20.0, 40.0, 80.0, 50.132),
        arguments(new double[] {9.5, 0.5}, 30.0, 40.0, 80.0, 81.831),

        arguments(new double[] {20.0, 1.0}, 1.0, 40.0, 80.0, 10.649),

        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0 - 10.0 / 200.0, 10.0, 30.0, 31.278),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0, 10.0, 30.0, 30.971),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0 - 10.0 / 200.0, 30.0, 50.0, 62.479),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0, 30.0, 50.0, 61.860),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0 - 10.0 / 200.0, 10.0, 50.0, 18.252),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 5.0, 10.0, 50.0, 18.069),

        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0 - 10.0 / 200.0, 10.0, 30.0, 16.821),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0, 10.0, 30.0, 16.761),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0 - 10.0 / 200.0, 30.0, 50.0, 32.383),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0, 30.0, 50.0, 32.246),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0 - 10.0 / 200.0, 10.0, 50.0, 9.118),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 10.0, 10.0, 50.0, 9.074),

        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0 - 10.0 / 200.0, 10.0, 30.0, 13.357),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0, 10.0, 30.0, 13.338),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0 - 10.0 / 200.0, 30.0, 50.0, 23.953),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0, 30.0, 50.0, 23.903),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0 - 10.0 / 200.0, 10.0, 50.0, 6.284),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 15.0, 10.0, 50.0, 6.267),

        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0 - 10.0 / 200.0, 10.0, 30.0, 12.194),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0, 10.0, 30.0, 12.187),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0 - 10.0 / 200.0, 30.0, 50.0, 20.589),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0, 30.0, 50.0, 20.567),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0 - 10.0 / 200.0, 10.0, 50.0, 5.090),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 20.0, 10.0, 50.0, 5.082),

        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0 - 10.0 / 200.0, 10.0, 30.0, 11.714),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0, 10.0, 30.0, 11.710),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0 - 10.0 / 200.0, 30.0, 50.0, 18.998),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0, 30.0, 50.0, 18.986),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0 - 10.0 / 200.0, 10.0, 50.0, 4.518),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 25.0, 10.0, 50.0, 4.514),

        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0 - 10.0 / 200.0, 10.0, 30.0, 11.484),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0, 10.0, 30.0, 11.482),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0 - 10.0 / 200.0, 30.0, 50.0, 18.158),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0, 30.0, 50.0, 18.152),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0 - 10.0 / 200.0, 10.0, 50.0, 4.218),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 30.0, 10.0, 50.0, 4.216),

        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0 - 10.0 / 200.0, 10.0, 30.0, 11.362),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0, 10.0, 30.0, 11.361),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0 - 10.0 / 200.0, 30.0, 50.0, 17.678),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0, 30.0, 50.0, 17.674),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0 - 10.0 / 200.0, 10.0, 50.0, 4.048),
        arguments(new double[] {0.7, Double.POSITIVE_INFINITY}, 35.0, 10.0, 50.0, 4.047)
    );
  }

  @ParameterizedTest
  @MethodSource("twoLayerParameters")
  void testLayer(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(smm), Metrics.fromMilli(lmm));
    assertThat(new Resistance2Layer(system).value(rho[0], rho[1], Metrics.fromMilli(hmm))).isCloseTo(rOhm, byLessThan(0.001));
    assertThat(TetrapolarResistance.ofMilli(smm, lmm).rho1(rho[0]).rho2(rho[1]).h(hmm).ohms()).isCloseTo(rOhm, byLessThan(0.001));
  }
}