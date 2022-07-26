package com.ak.rsm.inverse;

import java.util.List;
import java.util.stream.Stream;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import static com.ak.rsm.measurement.Measurements.fixOhms;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7701Provider {
  private InverseTestE7701Provider() {
  }

  static Stream<Arguments> e7701_14_30_08_s4() {
    double r1 = 218.0036;
    double r2 = 316.1553;
    double r3i = 145.5816;
    double r4i = 305.5205;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3934228, r2 + 0.50024171, r3i + 0.26890607, r4i + 0.5354764
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1769581, r2 + 0.22255399, r3i + 0.16128898, r4i + 0.2522647
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1006361, r2 + 0.09081233, r3i + 0.08451458, r4i + 0.1038049
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1215490, r2 - 0.11986143, r3i - 0.08715540, r4i - 0.1058499
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2839537, r2 - 0.32221848, r3i - 0.13285468, r4i - 0.2093415
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3984590, r2 - 0.41174297, r3i - 0.20934457, r4i - 0.3380304
                    ))
            )
        )
    );
  }

  static Stream<Arguments> e7701_14_31_24_s4() {
    double r1 = 219.939;
    double r2 = 319.1118;
    double r3i = 147.1011;
    double r4i = 308.4376;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4649077, r2 + 0.6263365, r3i + 0.3352219, r4i + 0.6698731
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2402705, r2 + 0.3197726, r3i + 0.1960956, r4i + 0.2847137
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1236470, r2 + 0.1501933, r3i + 0.1103584, r4i + 0.1279032
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1236611, r2 - 0.1416386, r3i - 0.1195941, r4i - 0.1335340
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3338592, r2 - 0.3815368, r3i - 0.2357522, r4i - 0.3544569
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.5761521, r2 - 0.7506620, r3i - 0.3588719, r4i - 0.5583971
                    ))
            )
        )
    );
  }

  static Stream<Arguments> e7701_14_33_36_s4() {
    double r1 = 216.1633;
    double r2 = 313.7265;
    double r3i = 144.1241;
    double r4i = 303.3973;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4308142, r2 + 0.33830639, r3i + 0.31694134, r4i + 0.48919909
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2734947, r2 + 0.18444718, r3i + 0.17442264, r4i + 0.23806702
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1160671, r2 + 0.07797762, r3i + 0.09756237, r4i + 0.08414657
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1073000, r2 - 0.10072993, r3i - 0.06024328, r4i - 0.05374332
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2293676, r2 - 0.13656457, r3i - 0.12815035, r4i - 0.15999509
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3506951, r2 - 0.24457940, r3i - 0.21356226, r4i - 0.28857252
                    ))
            )
        )
    );
  }
}
