package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7935Provider {
  private InverseTestE7935Provider() {
  }

  static Stream<Arguments> meat() {
    double r1 = 13.42265;
    double r2 = 23.09657;
    double r3 = 22.31912;
    double r4 = 23.47278;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.005335250, r2 - 0.01351325, r3 - 0.010730416, r4 - 0.014552158),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.003441269, r2 + 0.01180922, r3 + 0.008354656, r4 + 0.011471728)
            )
        )
    );
  }

  static Stream<Arguments> meat2() {
    double r1 = 7.35685;
    double r2 = 12.28508;
    double r3 = 11.995646;
    double r4 = 13.637414;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.00812974, r2 - 0.03369290, r3 - 0.012219934, r4 - 0.041237306),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.01586773, r2 + 0.06903237, r3 + 0.033469104, r4 + 0.088393816)
            )
        )
    );
  }

  static Stream<Arguments> fatMeat() {
    double r1 = 31.3435;
    double r2 = 61.69844;
    double r3 = 59.5578;
    double r4 = 80.03242;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1787885, r2 - 0.2927973, r3 - 0.3400244, r4 - 0.324666),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2148688, r2 + 0.3405826, r3 + 0.3746892, r4 + 0.4757694)
            )
        )
    );
  }

  static Stream<Arguments> fatMeat2() {
    double r1 = 93.47396;
    double r2 = 163.4075;
    double r3 = 167.33194;
    double r4 = 181.75026;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 1.295989, r2 - 1.598168, r3 - 2.640028, r4 - 0.586376),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 1.345541, r2 + 1.664787, r3 + 2.10514, r4 + 1.00144)
            )
        )
    );
  }
}
