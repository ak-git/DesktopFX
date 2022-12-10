package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static com.ak.rsm.measurement.Measurements.fixOhms;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7935Provider {
  private InverseTestE7935Provider() {
  }

  static Stream<Arguments> meat() {
    double r1 = 13.42265;
    double r2 = 23.09657;
    double r3i = 11.15956;
    double r4i = 22.89595;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.005335250, r2 - 0.01351325, r3i - 0.005365208, r4i - 0.012641287
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.003441269, r2 + 0.01180922, r3i + 0.004177328, r4i + 0.009913192
                    ))
            )
        )
    );
  }

  static Stream<Arguments> meat2() {
    double r1 = 7.35685;
    double r2 = 12.28508;
    double r3i = 5.997823;
    double r4i = 12.81653;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.00812974, r2 - 0.03369290, r3i - 0.006109967, r4i - 0.02672862
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.01586773, r2 + 0.06903237, r3i + 0.016734552, r4i + 0.06093146
                    ))
            )
        )
    );
  }

  static Stream<Arguments> fatMeat() {
    double r1 = 31.3435;
    double r2 = 61.69844;
    double r3i = 29.7789;
    double r4i = 69.79511;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1787885, r2 - 0.2927973, r3i - 0.1700122, r4i - 0.3323452
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2148688, r2 + 0.3405826, r3i + 0.1873446, r4i + 0.4252293
                    ))
            )
        )
    );
  }

  static Stream<Arguments> fatMeat2() {
    double r1 = 93.47396;
    double r2 = 163.4075;
    double r3i = 83.66597;
    double r4i = 174.5411;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 1.295989, r2 - 1.598168, r3i - 1.320014, r4i - 1.613202
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 1.345541, r2 + 1.664787, r3i + 1.052570, r4i + 1.553290
                    ))
            )
        )
    );
  }
}
