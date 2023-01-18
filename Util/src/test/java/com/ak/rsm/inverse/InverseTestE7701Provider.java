package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7701Provider {
  private InverseTestE7701Provider() {
  }

  static Stream<Arguments> e7701_14_30_08_s4() {
    double r1 = 218.0036;
    double r2 = 316.1553;
    double r3 = 291.1632;
    double r4 = 319.8778;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3934228, r2 + 0.50024171, r3 + 0.53781214, r4 + 0.53314066
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1769581, r2 + 0.22255399, r3 + 0.32257796, r4 + 0.18195144
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1006361, r2 + 0.09081233, r3 + 0.16902916, r4 + 0.03858064
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1215490, r2 - 0.11986143, r3 - 0.1743108, r4 - 0.037389
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2839537, r2 - 0.32221848, r3 - 0.26570936, r4 - 0.15297364
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3984590, r2 - 0.41174297, r3 - 0.41868914, r4 - 0.25737166
                    )
            )
        )
    );
  }

  static Stream<Arguments> e7701_14_31_24_s4() {
    double r1 = 219.939;
    double r2 = 319.1118;
    double r3 = 294.2022;
    double r4 = 322.6730;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4649077, r2 + 0.6263365, r3 + 0.6704438, r4 + 0.6693024
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2402705, r2 + 0.3197726, r3 + 0.3921912, r4 + 0.1772362
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1236470, r2 + 0.1501933, r3 + 0.2207168, r4 + 0.0350896
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1236611, r2 - 0.1416386, r3 - 0.2391882, r4 - 0.0278798
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3338592, r2 - 0.3815368, r3 - 0.4715044, r4 - 0.2374094
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.5761521, r2 - 0.7506620, r3 - 0.7177438, r4 - 0.3990504
                    )
            )
        )
    );
  }

  static Stream<Arguments> e7701_14_33_36_s4() {
    double r1 = 216.1633;
    double r2 = 313.7265;
    double r3 = 288.2482;
    double r4 = 318.5464;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4308142, r2 + 0.33830639, r3 + 0.63388268, r4 + 0.3445155
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2734947, r2 + 0.18444718, r3 + 0.34884528, r4 + 0.12728876
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1160671, r2 + 0.07797762, r3 + 0.19512474, r4 - 0.0268316
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1073000, r2 - 0.10072993, r3 - 0.12048656, r4 + 0.01299992
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2293676, r2 - 0.13656457, r3 - 0.2563007, r4 - 0.06368948
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3506951, r2 - 0.24457940, r3 - 0.42712452, r4 - 0.15002052
                    )
            )
        )
    );
  }
}
