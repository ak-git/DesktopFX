package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static com.ak.rsm.measurement.Measurements.fixOhms;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7940Provider {
    private InverseTestE7940Provider() {
    }

    static Stream<Arguments> fatMeat10() {
        double r1 = 52.55227;
        double r2 = 215.3735;
        double r3i = 42.98152;
        double r4i = 197.6395;
        return Stream.of(
                arguments(
                        List.of(
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                                        .ofOhms(fixOhms(
                                                r1, r2, r3i, r4i,
                                                r1 - 1.213047, r2 - 4.817362, r3i - 0.6211974, r4i - 3.892021
                                        )),
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                                        .ofOhms(fixOhms(
                                                r1, r2, r3i, r4i,
                                                r1 + 1.321576, r2 + 4.957581, r3i + 0.5954240, r4i + 4.526690
                                        ))
                        )
                )
        );
    }

    static Stream<Arguments> fatMeat08() {
        double r1 = 118.7855;
        double r2 = 319.3503;
        double r3i = 97.16263;
        double r4i = 268.2482;
        return Stream.of(
                arguments(
                        List.of(
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(8.0)
                                        .ofOhms(fixOhms(
                                                r1, r2, r3i, r4i,
                                                r1 - 5.161582, r2 - 10.679072, r3i - 3.341441, r4i - 6.949963
                                        )),
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(8.0)
                                        .ofOhms(fixOhms(
                                                r1, r2, r3i, r4i,
                                                r1 + 5.920044, r2 + 9.987513, r3i + 3.299464, r4i + 5.665814
                                        ))
                        )
                )
        );
    }

    static Stream<Arguments> fatMeat07() {
        double r1 = 82.43506;
        double r2 = 280.6788;
        double r3i = 49.13015;
        double r4i = 214.1656;
        return Stream.of(
                arguments(
                        List.of(
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(7.0)
                                        .ofOhms(fixOhms(
                                                r1, r2, r3i, r4i,
                                                r1 - 0.2598344, r2 - 0.6924013, r3i - 0.1087316, r4i - 0.3441716
                                        )),
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(7.0)
                                        .ofOhms(fixOhms(
                                                r1, r2, r3i, r4i,
                                                r1 + 0.4569713, r2 + 1.3941140, r3i + 0.1932548, r4i + 0.5611152
                                        ))
                        )
                )
        );
    }
}
