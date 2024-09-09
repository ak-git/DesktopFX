package com.ak.rsm.medium;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tech.units.indriya.unit.Units.PERCENT;

class Layer1MediumTest {
  static Stream<Arguments> layer1Medium() {
    return Stream.of(
        arguments(
            new Layer1Medium(TetrapolarMeasurement.milli(0.1).system4(7.0)
                .ofOhms(1.0, 2.0, 3.0, 4.0)),
            ValuePair.Name.RHO.of(0.0654, 0.00072)
        ),
        arguments(
            new Layer1Medium(TetrapolarDerivativeMeasurement.milli(0.1).dh(0.1).system4(7.0)
                .ofOhms(1.0, 2.0, 3.0, 4.0, 1.1, 2.2, 3.3, 4.4)),
            ValuePair.Name.RHO.of(0.0654, 0.00072)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("layer1Medium")
  void testRho(MediumLayers layers, ValuePair expected) {
    assertAll(layers.toString(),
        () -> assertThat(layers.rho()).isEqualTo(expected)
    );
  }


  @ParameterizedTest
  @MethodSource("layer1Medium")
  void testToString(Layer1Medium layers, ValuePair expected) {
    assertAll(layers.toString(),
        () -> assertThat(layers.toString()).contains(expected.toString()),
        () -> {
          double[] array = {0.3277112113340609, 0.10361494844541479, 0.5126497744983622, 0.6807219716648473};
          double rms = Arrays.stream(array).reduce(StrictMath::hypot).orElse(Double.NaN) / Math.sqrt(array.length);
          assertThat(layers.getRMS()).containsExactly(new double[] {rms}, byLessThan(0.001));
          assertThat(layers.toString()).contains("%.1f %%".formatted(Metrics.Dimensionless.ONE.to(rms, PERCENT)));
        }
    );
  }
}