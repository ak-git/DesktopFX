package com.ak.rsm.resistance;

import java.util.Arrays;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Resistance3LayerTest {
  static Stream<Arguments> threeLayerParameters() {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(10.0), Metrics.fromMilli(20.0));

    return Stream.of(
        arguments(new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 10.0, 20.0, 309.342),
        arguments(new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {0, 10}, 10.0, 20.0, 309.342),
        arguments(new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {10, 0}, 10.0, 20.0, 309.342),
        arguments(new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {10, 0}, 10.0, 20.0, 309.342),
        arguments(new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 309.342),
        arguments(new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(5), new int[] {2, 1}, 10.0, 20.0, 309.342),
        arguments(new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(10), new int[] {1, 5}, 10.0, 20.0, 309.342),

        arguments(
            new double[] {8.0, 8.0, 8.0}, Metrics.fromMilli(10), new int[] {1, 5}, 10.0, 20.0,
            new Resistance1Layer(system).value(8.0)
        ),
        arguments(
            new double[] {8.0, 8.0, 8.0}, Metrics.fromMilli(10), new int[] {0, 0}, 10.0, 20.0,
            new Resistance1Layer(system).value(8.0)
        ),
        arguments(
            new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(10), new int[] {30, 30}, 10.0, 20.0,
            new Resistance1Layer(system).value(8.0)
        ),

        arguments(
            new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0,
            new Resistance2Layer(system).value(8.0, 1.0, Metrics.fromMilli(5))
        ),
        arguments(new double[] {8.0, 2.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 242.751),
        arguments(new double[] {8.0, 3.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 257.079),
        arguments(new double[] {8.0, 4.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 269.694),
        arguments(new double[] {8.0, 5.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 281.017),
        arguments(
            new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0,
            new Resistance2Layer(system).value(8.0, 1.0, Metrics.fromMilli(10))
        ),

        arguments(
            new double[] {1.0, 1.0, 1.0}, Metrics.fromMilli(10), new int[] {1, 5}, 10.0, 20.0,
            new Resistance1Layer(system).value(1.0)
        ),

        arguments(
            new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(0.01), new int[] {1, 1}, 10.0, 20.0,
            new Resistance1Layer(system).value(1.0)
        ),

        arguments(new double[] {1.0, 1.0, 5.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 46.568),
        arguments(new double[] {1.0, 1.0, 8.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 47.403),
        arguments(new double[] {1.0, 5.0, 5.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 60.127),
        arguments(new double[] {1.0, 5.0, 8.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 61.435),
        arguments(new double[] {1.0, 8.0, 5.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 62.886),
        arguments(new double[] {1.0, 8.0, 8.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 64.048),

        arguments(new double[] {22.19, 1.57, 4.24}, Metrics.fromMilli(0.001), new int[] {154, 301}, 7.0, 7.0 * 5, 31.938),
        arguments(new double[] {22.19, 1.57, 4.24}, Metrics.fromMilli(0.001), new int[] {154, 301}, 7.0, 7.0 * 3, 94.584),
        arguments(new double[] {22.19, 1.57, 4.24}, Metrics.fromMilli(0.001), new int[] {154, 301}, 7.0 * 3, 7.0 * 5, 142.539),

        arguments(new double[] {10.0, 5.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 99.949),// briko: 101.99
        arguments(new double[] {5.0, 10.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 103.657),// briko: 104.87
        arguments(new double[] {1.0, 5.0, 10.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 49.651),// briko: 53.11

        arguments(new double[] {10.0, 1.0, 10.0}, Metrics.fromMilli(0.1), new int[] {3_0, 1}, 10.0, 30.0, 149.637),
        arguments(new double[] {10.0, 1.0, 10.0}, Metrics.fromMilli(0.01), new int[] {3_00, 10}, 10.0, 30.0, 149.637),
        arguments(new double[] {10.0, 1.0, 10.0}, Metrics.fromMilli(0.1), new int[] {1000, 1}, 10.0, 30.0, 159.154)
    );
  }

  @ParameterizedTest
  @MethodSource("threeLayerParameters")
  void testLayer(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                 @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(smm), Metrics.fromMilli(lmm));
    assertAll(Arrays.toString(rho),
        () -> assertThat(new Resistance3Layer(system, hStepSI).value(rho[0], rho[1], rho[2], p[0], p[1]))
            .isCloseTo(rOhm, byLessThan(0.001)),
        () -> assertThat(TetrapolarResistance
            .ofMilli(smm, lmm)
            .rho1(rho[0]).rho2(rho[1]).rho3(rho[2]).hStep(Metrics.toMilli(hStepSI)).p(p[0], p[1]).ohms())
            .isCloseTo(rOhm, byLessThan(0.001))
    );
  }

  static Stream<Arguments> threeLayerParametersSpecial() {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(10.0), Metrics.fromMilli(30.0));
    return Stream.of(
        arguments(new double[] {10.0, 1.0, 1.0}, Metrics.fromMilli(0.1), new int[] {10, 0}, 10.0, 30.0,
            new Resistance2Layer(system).value(10.0, 1.0, Metrics.fromMilli(1))),
        arguments(new double[] {1.0, 10.0, 1.0}, Metrics.fromMilli(0.1), new int[] {0, 10}, 10.0, 30.0,
            new Resistance2Layer(system).value(10.0, 1.0, Metrics.fromMilli(1))),
        arguments(new double[] {1.0, 1.0, 10.0}, Metrics.fromMilli(0.1), new int[] {0, 0}, 10.0, 30.0,
            new Resistance1Layer(system).value(10.0))
    );
  }

  @ParameterizedTest
  @MethodSource("threeLayerParametersSpecial")
  void testLayerSpecial(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                        @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(smm), Metrics.fromMilli(lmm));
    assertAll(Arrays.toString(rho),
        () -> assertThat(new Resistance3Layer(system, hStepSI).value(rho[0], rho[1], rho[2], p[0], p[1]))
            .isCloseTo(rOhm, byLessThan(0.001)),
        () -> assertThat(TetrapolarResistance
            .ofMilli(smm, lmm)
            .rho1(rho[0]).rho2(rho[1]).rho3(rho[2]).hStep(Metrics.toMilli(hStepSI)).p(p[0], p[1]).ohms())
            .isCloseTo(rOhm, byLessThan(0.001))
    );
  }
}