package com.ak.rsm.medium;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Layer1MediumTest {
  static Stream<Arguments> layer1Medium() {
    return Stream.of(
        arguments(
            new Layer1Medium(TetrapolarMeasurement.milli(0.1).system4(7.0).ofOhms(1.0, 2.0, 3.0, 4.0)),
            ValuePair.Name.RHO_1.of(0.0654, 0.00072)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("layer1Medium")
  @ParametersAreNonnullByDefault
  void testRho(MediumLayers layers, ValuePair expected) {
    assertAll(layers.toString(),
        () -> assertThat(layers.rho()).isEqualTo(expected),
        () -> assertThat(layers.rho1()).isEqualTo(expected),
        () -> assertThat(layers.rho2()).isEqualTo(expected)
    );
  }

  @ParameterizedTest
  @MethodSource("layer1Medium")
  @ParametersAreNonnullByDefault
  void testH(MediumLayers layers, ValuePair expected) {
    assertThat(layers.h1().value()).isNaN();
    assertNotNull(expected);
  }

  @ParameterizedTest
  @MethodSource("layer1Medium")
  @ParametersAreNonnullByDefault
  void testToString(MediumLayers layers, ValuePair expected) {
    assertAll(layers.toString(),
        () -> assertThat(layers.toString()).contains(expected.toString()),
        () -> {
          double[] array = {0.3277112113340609, 0.10361494844541479, 0.5126497744983622, 0.6807219716648473};
          double rms = Arrays.stream(array).reduce(StrictMath::hypot).orElse(Double.NaN) / Math.sqrt(array.length);
          assertThat(layers.getRMS()).containsExactly(new double[] {rms}, byLessThan(0.001));
          assertThat(layers.toString()).contains("%.1f %%".formatted(Metrics.toPercents(rms)));
        }
    );
  }
}