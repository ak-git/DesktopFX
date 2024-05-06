package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7694akProvider {
  private InverseTestE7694akProvider() {
  }

  static Stream<Arguments> e17_52_54_s4() {
    double r1 = 123.0933;
    double r2 = 200.2154;
    double r3 = 132.17826;
    double r4 = 271.76674;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3080655, r2 - 1.2502451, r3 - 0.7826758, r4 - 1.5915058
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1657916, r2 - 0.6687096, r3 - 0.3425554, r4 - 0.6475226
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1233832, r2 - 0.3268607, r3 - 0.2467832, r4 - 0.4228932
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1073339, r2 + 0.3525168, r3 + 0.2452476, r4 + 0.4444754
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2730492, r2 + 0.7895464, r3 + 0.5151086, r4 + 1.0757256),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.7467654, r2 + 2.0424332, r3 + 1.3847212, r4 + 2.6294078)
            )
        )
    );
  }

  static Stream<Arguments> e17_55_32_s4() {
    double r1 = 120.9013;
    double r2 = 195.1735;
    double r3 = 133.88032;
    double r4 = 262.84428;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.5432740, r2 - 1.5530896, r3 - 0.9735416, r4 - 1.9936772
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3126845, r2 - 0.8661197, r3 - 0.5099698, r4 - 1.1616666
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1417560, r2 - 0.4326083, r3 - 0.2654072, r4 - 0.6035114
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1605793, r2 + 0.4984282, r3 + 0.28445, r4 + 0.6997756
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4441669, r2 + 1.1758138, r3 + 0.7228856, r4 + 1.5995846
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 1.1854495, r2 + 2.8287508, r3 + 1.813304, r4 + 3.560943
                    )
            )
        )
    );
  }

  static Stream<Arguments> e17_56_55_s4() {
    double r1 = 123.2331;
    double r2 = 197.9802;
    double r3 = 136.08332;
    double r4 = 269.22148;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 1.1246444, r2 - 2.8743403, r3 - 1.3791232, r4 - 5.1838634
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.6166534, r2 - 1.6125186, r3 - 0.8717094, r4 - 3.1361826
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3266737, r2 - 0.8489396, r3 - 0.3885294, r4 - 1.5362744
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3751665, r2 + 0.9418706, r3 + 0.4087682, r4 + 1.5577498
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.9938925, r2 + 2.1200056, r3 + 1.0669204, r4 + 3.7129844
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 2.4238733, r2 + 4.9135797, r3 + 2.6844502, r4 + 8.8565216
                    )
            )
        )
    );
  }

  static Stream<Arguments> e17_58_47_s4() {
    double r1 = 117.3288;
    double r2 = 184.423;
    double r3 = 128.17358;
    double r4 = 247.44542;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.9784932, r2 - 2.5263704, r3 - 1.6358886, r4 - 2.6780734
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3936637, r2 - 1.1291288, r3 - 0.999862, r4 - 1.5049252
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2395596, r2 - 0.6700464, r3 - 0.4397682, r4 - 0.6608446
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2728100, r2 + 0.7996640, r3 + 0.4827286, r4 + 0.8747464
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4644685, r2 + 1.4363729, r3 + 1.1427196, r4 + 1.857783
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 1.6204279, r2 + 3.8948370, r3 + 2.9921824, r4 + 4.5606628
                    )
            )
        )
    );
  }

  static Stream<Arguments> e18_23_57_s4() {
    double r1 = 136.233;
    double r2 = 210.4502;
    double r3 = 171.71316;
    double r4 = 251.05564;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 1.0305256, r2 - 2.9120818, r3 - 2.6490274, r4 - 4.2273246
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.5980421, r2 - 1.6658970, r3 - 1.5511924, r4 - 2.6960096
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3513220, r2 - 0.9819082, r3 - 0.875123, r4 - 1.651767
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4298199, r2 + 1.0972971, r3 + 0.8266734, r4 + 1.6704426
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.8905175, r2 + 2.2933112, r3 + 1.8103542, r4 + 4.3670918
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 2.3792565, r2 + 5.5974700, r3 + 4.673243, r4 + 11.986041
                    )
            )
        )
    );
  }

  static Stream<Arguments> e18_27_01_s4() {
    double r1 = 124.2859;
    double r2 = 185.9248;
    double r3 = 157.13836;
    double r4 = 207.79984;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.4166812, r2 - 2.0003750, r3 - 1.4642766, r4 - 2.243686
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2695041, r2 - 1.1184466, r3 - 0.7578756, r4 - 1.1533784
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1529194, r2 - 0.6168080, r3 - 0.457178, r4 - 0.7137758
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1612338, r2 + 0.6404985, r3 + 0.507352, r4 + 0.6823162
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4109427, r2 + 1.4083486, r3 + 0.9875716, r4 + 1.679086
                    ),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4109427, r2 + 1.4083486, r3 + 2.3451882, r4 + 4.5869304
                    )
            )
        )
    );
  }
}
