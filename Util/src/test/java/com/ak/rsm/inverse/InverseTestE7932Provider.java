package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7932Provider {
  private InverseTestE7932Provider() {
  }

  static Stream<Arguments> meatFat() {
    double r1 = 19.47067;
    double r2 = 35.88068;
    double r3 = 33.76976;
    double r4 = 47.19336;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.5115441, r2 - 0.4903946, r3 - 0.4271542, r4 - 1.0080928),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3291593, r2 - 0.3336468, r3 - 0.242462, r4 - 0.6640342),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2234464, r2 + 0.3055168, r3 + 0.20374, r4 + 0.5888716),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.5676624, r2 + 0.8385997, r3 + 0.5989682, r4 + 1.4780112)
            )
        )
    );
  }

  static Stream<Arguments> meat() {
    double r1 = 5.619337;
    double r2 = 8.976139;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system2(10.0)
                    .ofOhms(r1, r2, r1 - 0.02665655, r2 - 0.010386613),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system2(10.0)
                    .ofOhms(r1, r2, r1 - 0.01803588, r2 - 0.004433184),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(10.0)
                    .ofOhms(r1, r2, r1 + 0.01107191, r2 + 0.005857321),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(10.0)
                    .ofOhms(r1, r2, r1 + 0.04725428, r2 + 0.024173583)
            )
        )
    );
  }
}
