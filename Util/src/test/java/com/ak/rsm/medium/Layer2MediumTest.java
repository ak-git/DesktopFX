package com.ak.rsm.medium;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.util.Metrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tec.uom.se.unit.Units.METRE;

class Layer2MediumTest {
  static Stream<Arguments> layer2Medium() {
    Collection<Measurement> measurements = TetrapolarMeasurement
        .milli(0.1).system2(7.0).ofOhms(1.0, 2.0);
    return Stream.of(
        arguments(
            new Layer2Medium(measurements, new RelativeMediumLayers(
                ValuePair.Name.K12.of(0.6, 0.01),
                ValuePair.Name.H_L.of(0.2, 0.01))
            ),
            new double[] {0.0522, 0.0287, 0.1149, Metrics.Length.MILLI.to(7.0 * 3 * 0.2, METRE)}
        ),
        arguments(
            new Layer2Medium(measurements, new RelativeMediumLayers(
                ValuePair.Name.K12.of(-0.6, 0.001),
                ValuePair.Name.H_L.of(0.1, 0.001))
            ),
            new double[] {0.0522, 0.1601, 0.0400, Metrics.Length.MILLI.to(7.0 * 3 * 0.1, METRE)}
        ),
        arguments(
            new Layer2Medium(measurements, RelativeMediumLayers.SINGLE_LAYER),
            new double[] {0.0522, 0.0522, 0.0522, Double.NaN}
        ),
        arguments(
            new Layer2Medium(measurements, RelativeMediumLayers.NAN),
            new double[] {0.0522, Double.NaN, Double.NaN, Double.NaN}
        )
    );
  }

  static Stream<Arguments> layer2MediumDerivative() {
    Collection<DerivativeMeasurement> measurements = TetrapolarDerivativeMeasurement
        .milli(0.1).dh(-0.1).system2(7.0).ofOhms(1.0, 2.0, 1.1, 2.2);
    return Stream.of(
        arguments(
            new Layer2Medium(measurements, new RelativeMediumLayers(
                ValuePair.Name.K12.of(0.6, 0.01),
                ValuePair.Name.H_L.of(0.2, 0.01))
            ),
            new double[] {0.0522, 0.0291, 0.1166, Metrics.Length.MILLI.to(7.0 * 3 * 0.2, METRE)}
        ),
        arguments(
            new Layer2Medium(measurements, new RelativeMediumLayers(
                ValuePair.Name.K12.of(-0.6, 0.001),
                ValuePair.Name.H_L.of(0.1, 0.001))
            ),
            new double[] {0.0522, 0.1418, 0.0354, Metrics.Length.MILLI.to(7.0 * 3 * 0.1, METRE)}
        ),
        arguments(
            new Layer2Medium(measurements, RelativeMediumLayers.SINGLE_LAYER),
            new double[] {0.0522, 0.0522, 0.0522, Double.NaN}
        )
    );
  }

  @ParameterizedTest
  @MethodSource({"layer2Medium", "layer2MediumDerivative"})
  @ParametersAreNonnullByDefault
  void testRho(Layer2Medium layers, double[] expected) {
    Assertions.assertAll(layers.toString(), () -> {
      assertThat(layers.rho().value()).isCloseTo(expected[0], byLessThan(0.001));
      assertThat(layers.rho1().value()).isCloseTo(expected[1], byLessThan(0.001));
      assertThat(layers.rho2().value()).isCloseTo(expected[2], byLessThan(0.001));
      assertThat(layers.h().value()).isCloseTo(expected[3], byLessThan(0.001));
    });
  }

  @ParameterizedTest
  @MethodSource({"layer2Medium", "layer2MediumDerivative"})
  @ParametersAreNonnullByDefault
  void testToString(Layer2Medium layers, double[] expected) {
    Arrays.stream(expected).limit(2).forEach(value -> {
      if (Double.isFinite(value)) {
        assertThat(layers.toString()).contains("%.4f".formatted(value));
      }
      else {
        assertThat(layers.toString()).doesNotContain("%.4f".formatted(value));
      }
    });

    Arrays.stream(expected).skip(2).forEach(value -> {
      if (Double.isFinite(value)) {
        assertThat(layers.toString()).contains("%.1f".formatted(value));
      }
      else {
        assertThat(layers.toString()).doesNotContain("%.1f".formatted(value));
      }
    });
  }
}
