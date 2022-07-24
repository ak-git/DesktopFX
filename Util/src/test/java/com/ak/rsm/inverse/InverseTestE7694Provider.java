package com.ak.rsm.inverse;

import java.util.List;
import java.util.stream.Stream;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import static com.ak.rsm.measurement.Measurements.fixOhms;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7694Provider {
  private InverseTestE7694Provider() {
  }

  static Stream<Arguments> e7696_17_52_54_s4() {
    double r1 = 123.0933;
    double r2 = 200.2154;
    double r3i = 66.08913;
    double r4i = 201.9725;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3080655, r2 - 1.2502451, r3i - 0.3913379, r4i - 1.1870908
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1657916, r2 - 0.6687096, r3i - 0.1712777, r4i - 0.4950390
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1233832, r2 - 0.3268607, r3i - 0.1233916, r4i - 0.3348382
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1073339, r2 + 0.3525168, r3i + 0.1226238, r4i + 0.3448615
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2730492, r2 + 0.7895464, r3i + 0.2575543, r4i + 0.7954171
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.7467654, r2 + 2.0424332, r3i + 0.6923606, r4i + 2.0070645
                    ))
            )
        )
    );
  }

  static Stream<Arguments> e7696_17_55_32_s4() {
    double r1 = 120.9013;
    double r2 = 195.1735;
    double r3i = 66.94016;
    double r4i = 198.3623;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.5432740, r2 - 1.5530896, r3i - 0.4867708, r4i - 1.4836094
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3126845, r2 - 0.8661197, r3i - 0.2549849, r4i - 0.8358182
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1417560, r2 - 0.4326083, r3i - 0.1327036, r4i - 0.4344593
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1605793, r2 + 0.4984282, r3i + 0.1422250, r4i + 0.4921128
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4441669, r2 + 1.1758138, r3i + 0.3614428, r4i + 1.1612351
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 1.1854495, r2 + 2.8287508, r3i + 0.9066520, r4i + 2.6871235
                    ))
            )
        )
    );
  }

  static Stream<Arguments> e7696_17_56_55_s4() {
    double r1 = 123.2331;
    double r2 = 197.9802;
    double r3i = 68.04166;
    double r4i = 202.6524;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 1.1246444, r2 - 2.8743403, r3i - 0.6895616, r4i - 3.2814933
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.6166534, r2 - 1.6125186, r3i - 0.4358547, r4i - 2.0039460
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3266737, r2 - 0.8489396, r3i - 0.1942647, r4i - 0.9624019
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3751665, r2 + 0.9418706, r3i + 0.2043841, r4i + 0.9832590
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.9938925, r2 + 2.1200056, r3i + 0.5334602, r4i + 2.3899524
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 2.4238733, r2 + 4.9135797, r3i + 1.3422251, r4i + 5.7704859
                    ))
            )
        )
    );
  }

  static Stream<Arguments> e7696_17_58_47_s4() {
    double r1 = 117.3288;
    double r2 = 184.423;
    double r3i = 64.08679;
    double r4i = 187.8095;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.9784932, r2 - 2.5263704, r3i - 0.8179443, r4i - 2.1569810
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3936637, r2 - 1.1291288, r3i - 0.4999310, r4i - 1.2523936
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2395596, r2 - 0.6700464, r3i - 0.2198841, r4i - 0.5503064
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2728100, r2 + 0.7996640, r3i + 0.2413643, r4i + 0.6787375
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4644685, r2 + 1.4363729, r3i + 0.5713598, r4i + 1.5002513
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 1.6204279, r2 + 3.8948370, r3i + 1.4960912, r4i + 3.7764226
                    ))
            )
        )
    );
  }

  static Stream<Arguments> e7696_18_23_57_s4() {
    double r1 = 136.233;
    double r2 = 210.4502;
    double r3i = 85.85658;
    double r4i = 211.3844;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 1.0305256, r2 - 2.9120818, r3i - 1.3245137, r4i - 3.438176
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.5980421, r2 - 1.6658970, r3i - 0.7755962, r4i - 2.123601
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3513220, r2 - 0.9819082, r3i - 0.4375615, r4i - 1.263445
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4298199, r2 + 1.0972971, r3i + 0.4133367, r4i + 1.248558
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.8905175, r2 + 2.2933112, r3i + 0.9051771, r4i + 3.088723
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 2.3792565, r2 + 5.5974700, r3i + 2.3366215, r4i + 8.329642
                    ))
            )
        )
    );
  }

  static Stream<Arguments> e7696_18_27_01_s4() {
    double r1 = 124.2859;
    double r2 = 185.9248;
    double r3i = 78.56918;
    double r4i = 182.4691;

    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.4166812, r2 - 2.0003750, r3i - 0.7321383, r4i - 1.8539813
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2695041, r2 - 1.1184466, r3i - 0.3789378, r4i - 0.9556270
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1529194, r2 - 0.6168080, r3i - 0.2285890, r4i - 0.5854769
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1612338, r2 + 0.6404985, r3i + 0.2536760, r4i + 0.5948341
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4109427, r2 + 1.4083486, r3i + 0.4937858, r4i + 1.3333288
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4109427, r2 + 1.4083486, r3i + 1.1725941, r4i + 3.4660593
                    ))
            )
        )
    );
  }
}
