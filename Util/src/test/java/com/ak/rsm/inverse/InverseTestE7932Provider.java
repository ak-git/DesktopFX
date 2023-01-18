package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static com.ak.rsm.measurement.Measurements.fixOhms;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7932Provider {
  private InverseTestE7932Provider() {
  }

  static Stream<Arguments> meatFat() {
    double r1 = 19.47067;
    double r2 = 35.88068;
    double r3i = 16.88488;
    double r4i = 40.48156;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.5115441, r2 - 0.4903946, r3i - 0.2135771, r4i - 0.7176235
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3291593, r2 - 0.3336468, r3i - 0.1212310, r4i - 0.4532481
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2234464, r2 + 0.3055168, r3i + 0.1018700, r4i + 0.3963058
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.5676624, r2 + 0.8385997, r3i + 0.2994841, r4i + 1.0384897
                    ))
            )
        )
    );
  }

  static Stream<Arguments> meat() {
    double r1 = 5.619337;
    double r2 = 8.976139;
    double r3i = 4.31785;
    double r4i = 9.188934;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.02665655, r2 - 0.010386613, r3i - 0.016149800, r4i - 0.010249956
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.01803588, r2 - 0.004433184, r3i - 0.008536334, r4i - 0.008540883
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.01107191, r2 + 0.005857321, r3i + 0.001531586, r4i + 0.013473002
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.04725428, r2 + 0.024173583, r3i + 0.045525415, r4i + 0.031669155
                    ))
            )
        )
    );
  }
}
