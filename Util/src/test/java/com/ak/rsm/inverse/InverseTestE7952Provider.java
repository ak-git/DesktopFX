package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static com.ak.rsm.measurement.Measurements.fixOhms;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7952Provider {
    private InverseTestE7952Provider() {
    }

    static Stream<Arguments> fatMeat10() {
        double r1 = 181.5504;
        double r2 = 248.7534;
        double r3i = 114.7527;
        double r4i = 246.4465;
        return Stream.of(
                arguments(
                        List.of(
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                                        .ofOhms(fixOhms(
                                                r1, r2, r3i, r4i,
                                                r1 - 0.06740410, r2 - 0.1564458, r3i - 0.05248565, r4i - 0.1595572
                                        )),
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                                        .ofOhms(fixOhms(
                                                r1, r2, r3i, r4i,
                                                r1 + 0.07318971, r2 + 0.1783049, r3i + 0.05579101, r4i + 0.1695431
                                        ))
                        )
                )
        );
    }

    static Stream<Arguments> fatMeat10_2() {
        double r1 = 178.9135;
        double r2 = 246.32;
        return Stream.of(
                arguments(
                        List.of(
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system2(10.0)
                                        .ofOhms(fixOhms(
                                                r1, r2,
                                                r1 - 0.04683035, r2 - 0.07941054
                                        )),
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(10.0)
                                        .ofOhms(fixOhms(
                                                r1, r2,
                                                r1 + 0.03921637, r2 + 0.08798873
                                        ))
                        )
                )
        );
    }

    static Stream<Arguments> fatMeat08() {
        double r1 = 218.206;
        double r2 = 302.9703;
        double r3i = 145.387;
        double r4i = 296.7358;
        return Stream.of(
                arguments(
                        List.of(
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(8.0)
                                        .ofOhms(fixOhms(
                                                r1, r2, r3i, r4i,
                                                r1 - 0.0983881, r2 - 0.1649697, r3i - 0.07161030, r4i - 0.1594080
                                        )),
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(8.0)
                                        .ofOhms(fixOhms(
                                                r1, r2, r3i, r4i,
                                                r1 + 0.0808309, r2 + 0.1641904, r3i + 0.07049467, r4i + 0.1776698
                                        ))
                        )
                )
        );
    }
}
