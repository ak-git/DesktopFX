package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7940Provider {
  private InverseTestE7940Provider() {
  }

  static Stream<Arguments> fatMeat10() {
    double r1 = 52.55227;
    double r2 = 215.3735;
    double r3 = 85.96304;
    double r4 = 309.31596;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 1.213047, r2 - 4.817362, r3 - 1.2423948, r4 - 6.5416472
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 1.321576, r2 + 4.957581, r3 + 1.190848, r4 + 7.862532
                    )
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system2(10.0)
                    .ofOhms(r1, r2, r1 - 1.213047, r2 - 4.817362),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(10.0)
                    .ofOhms(r1, r2, r1 + 1.321576, r2 + 4.957581)
            )
        )
    );
  }

  static Stream<Arguments> fatMeat08() {
    double r1 = 118.7855;
    double r2 = 319.3503;
    double r3 = 194.32526;
    double r4 = 342.17114;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 5.161582, r2 - 10.679072, r3 - 6.682882, r4 - 7.217044),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 5.920044, r2 + 9.987513, r3 + 6.598928, r4 + 4.7327)
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system2(8.0)
                    .ofOhms(r1, r2, r1 - 5.161582, r2 - 10.679072),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(8.0)
                    .ofOhms(r1, r2, r1 + 5.920044, r2 + 9.987513)
            )
        )
    );
  }

  static Stream<Arguments> fatMeat07() {
    double r1 = 82.43506;
    double r2 = 280.6788;
    double r3 = 98.2603;
    double r4 = 330.0709;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2598344, r2 - 0.6924013, r3 - 0.2174632, r4 - 0.47088
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4569713, r2 + 1.3941140, r3 + 0.3865096, r4 + 0.7357208
                    )
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system2(7.0)
                    .ofOhms(r1, r2, r1 - 0.2598344, r2 - 0.6924013),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(7.0)
                    .ofOhms(r1, r2, r1 + 0.4569713, r2 + 1.3941140)
            )
        )
    );
  }
}
