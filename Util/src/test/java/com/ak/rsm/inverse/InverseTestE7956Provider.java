package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7956Provider {
    private InverseTestE7956Provider() {
    }

    static Stream<Arguments> meat() {
        return Stream.of(arguments(TetrapolarMeasurement.milli(0.1).system4(10.0)
                .ofOhms(52.73636, 136.7824, 95.36825, 178.7174))
        );
    }

    static Stream<Arguments> fatSkinBottom() {
        return Stream.of(arguments(TetrapolarMeasurement.milli(0.1).system4(10.0)
                .ofOhms(57.85299, 141.4908, 99.82111, 184.0643))
        );
    }

    static Stream<Arguments> fatSkinTop() {
        return Stream.of(arguments(TetrapolarMeasurement.milli(0.1).system4(10.0)
                .ofOhms(169.5547, 267.1312, 229.2745, 300.7458))
        );
    }

    static Stream<Arguments> fatSkinBottom2() {
        double r1 = 57.85299;
        double r2 = 141.4908;
        double r3i = 99.82111;
        double r4i = 184.0643;
        return Stream.of(
                arguments(
                        List.of(
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                                        .ofOhms(r1, r2, r3i, r4i,
                                                r1 + 0.1514031, r2 + 0.2844851, r3i + 0.2068252, r4i + 0.2975081
                                        ),
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                                        .ofOhms(r1, r2, r3i, r4i,
                                                r1 - 0.1536157, r2 - 0.2829993, r3i - 0.2103738, r4i - 0.2964236
                                        )
                        )
                )
        );
    }

    static Stream<Arguments> meatFat() {
        double r1 = 45.5515;
        double r2 = 86.12099;
        double r3i = 71.48387;
        double r4i = 101.6155;
        return Stream.of(
                arguments(
                        List.of(
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105).system4(10.0)
                                        .ofOhms(r1, r2, r3i, r4i,
                                                r1 + 0.03069195, r2 + 0.06970074, r3i + 0.04122427, r4i + 0.08490068
                                        ),
                                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system4(10.0)
                                        .ofOhms(r1, r2, r3i, r4i,
                                                r1 - 0.02978715, r2 - 0.06716437, r3i - 0.04176568, r4i - 0.10025700
                                        )
                        )
                )
        );
    }
}
