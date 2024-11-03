package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * <pre>
 *   [1] "1.17 mm; 134  -  144 s"
 *   [1] "120.5732518, 174.7445165, 0.106153846153846, 0.347923076923073"
 * </pre>
 * <pre>
 *   [1] "1.08 mm; 145  -  154 s"
 *   [1] "120.449686111111, 174.400443444444, 0.100999999999997, 0.338771428571425"
 * </pre>
 */
class InverseTestE9396akProvider {
  private InverseTestE9396akProvider() {
  }

  static Stream<Arguments> e12() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.09).system2(7.0)
                .ofOhms(120.5732518, 174.7445165, 120.5732518 + 0.106153846153846, 174.7445165 + 0.347923076923073),
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.09).system2(7.0)
                .ofOhms(120.449686111111, 174.400443444444, 120.449686111111 + 0.100999999999997, 174.400443444444 + 0.338771428571425),
            1.17 - 1.08
        )
    );
  }
}
