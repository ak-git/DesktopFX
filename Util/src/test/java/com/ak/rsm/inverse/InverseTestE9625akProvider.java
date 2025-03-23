package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.resistance.DeltaH;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE9625akProvider {
  private InverseTestE9625akProvider() {
  }

  static Stream<Arguments> e1() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(DeltaH.H1.apply(0.090)).system2(7.0)
                .ofOhms(129.0, 195.0, 129.0 + 0.2, 195.0 + 0.5),
            TetrapolarDerivativeMeasurement.milli(0.1).dh(DeltaH.H1.apply(0.090)).system2(7.0)
                .ofOhms(128.9, 195.5, 128.9 + 0.2, 195.5 + 0.5)
        )
    );
  }
}
