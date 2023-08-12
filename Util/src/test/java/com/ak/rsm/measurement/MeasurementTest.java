package com.ak.rsm.measurement;

import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnull;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MeasurementTest {

  @RepeatedTest(10)
  void average() {
    double randomRho = new SecureRandom().nextDouble(10.0) + 1.0;
    double sPUmm = 7.0;
    var measurements = TetrapolarMeasurement.milli(0.1).system2(sPUmm)
        .ofOhms(TetrapolarResistance.milli().system2(sPUmm).rho(randomRho, randomRho).stream().mapToDouble(Resistance::ohms).toArray());
    assertThat(Measurement.average(measurements))
        .satisfies(average -> assertThat(average.resistivity())
            .isCloseTo(randomRho, byLessThan(average.resistivity() * average.inexact().getApparentRelativeError()))
        );
  }


  static Stream<Arguments> measurements() {
    return Stream.of(
        arguments(TetrapolarMeasurement.milli(0.1).system4(6.0)
            .ofOhms(100.0, 200.0, 300.0, 400.0), 6.0 * 4),
        arguments(TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
            .ofOhms(122.3, 199.0, 122.3 + 0.1, 199.0 + 0.4), 7.0 * 3)
    );
  }

  @ParameterizedTest
  @MethodSource("measurements")
  void inexact(@Nonnull Collection<? extends Measurement> measurements) {
    assertThat(Measurement.inexact(measurements)).hasSameElementsAs(measurements.stream().map(Measurement::inexact).toList());
  }
}