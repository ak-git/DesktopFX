package com.ak.rsm.medium;

import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Layer2MediumTest {
  static Stream<Arguments> layer2Medium() {
    Collection<Measurement> measurements = TetrapolarMeasurement.milli(0.1).system2(7.0).ofOhms(1.0, 2.0);
    return Stream.of(
        arguments(
            new Layer2Medium(measurements, new Layer2RelativeMedium(
                ValuePair.Name.K12.of(0.6, 0.01),
                ValuePair.Name.H_L.of(0.2, 0.01))
            ),
            new double[] {0.0287, 0.1149, Metrics.fromMilli(7.0 * 3 * 0.2)}
        ),
        arguments(
            new Layer2Medium(measurements, new Layer2RelativeMedium(
                ValuePair.Name.K12.of(-0.6, 0.001),
                ValuePair.Name.H_L.of(0.1, 0.001))
            ),
            new double[] {0.1601, 0.0400, Metrics.fromMilli(7.0 * 3 * 0.1)}
        ),
        arguments(
            new Layer2Medium(measurements, RelativeMediumLayers.SINGLE_LAYER),
            new double[] {0.0522, 0.0522, Double.NaN}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("layer2Medium")
  @ParametersAreNonnullByDefault
  void testRho(MediumLayers layers, double[] expected) {
    assertThat(layers.rho().value()).isCloseTo(expected[0], byLessThan(0.001));
    assertThat(layers.rho1().value()).isCloseTo(expected[0], byLessThan(0.001));
    assertThat(layers.rho2().value()).isCloseTo(expected[1], byLessThan(0.001));
    assertThat(layers.h1().value()).isCloseTo(expected[2], byLessThan(0.001));
  }

  @ParameterizedTest
  @MethodSource("layer2Medium")
  @ParametersAreNonnullByDefault
  void testToString(MediumLayers layers, double[] expected) {
    assertThat(layers.toString())
        .contains("%.4f".formatted(expected[0]))
        .contains("%.1f".formatted(expected[1]))
        .contains("%.1f".formatted(expected[2]));
  }
}
