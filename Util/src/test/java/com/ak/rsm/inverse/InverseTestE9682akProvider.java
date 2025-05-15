package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class InverseTestE9682akProvider {
  private InverseTestE9682akProvider() {
  }

  static Stream<Arguments> e9682model() {
    return Stream.of(
        Arguments.arguments(
            TetrapolarMeasurement.milli(0.1).system4(7.0).rho1(8.0).rho2(4.0).h(4.0),
            TetrapolarMeasurement.milli(0.1).system4(7.0).rho1(8.0).rho2(4.01).h(4.0)
        )
    );
  }

  static Stream<Arguments> e9682force() {
    return Stream.of(
        Arguments.arguments(
            TetrapolarMeasurement.milli(0.1).system4(7.0).ofOhms(118.644, 167.818, 156.530, 170.554).stream().toList(),
            TetrapolarMeasurement.milli(0.1).system4(7.0).ofOhms(118.998, 168.531, 156.784, 172.328).stream().toList()
        )
    );
  }
}
