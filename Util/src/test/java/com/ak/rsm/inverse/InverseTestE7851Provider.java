package com.ak.rsm.inverse;

import java.util.List;
import java.util.stream.Stream;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import static com.ak.rsm.measurement.Measurements.fixOhms;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7851Provider {
  private InverseTestE7851Provider() {
  }

  /**
   * <b>- 1.05 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     62-85 s
   *   </li>
   *   <li>
   *     88-104 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_105() {
    double r1 = 137.9309;
    double r2 = 211.3749;
    double r3i = 91.2834;
    double r4i = 207.9285;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.21813130, r2 - 0.40941236, r3i + 0.043962738, r4i - 0.3998953
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.15973889, r2 - 0.20821824, r3i + 0.008426390, r4i - 0.2527309
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.07675139, r2 - 0.08883005, r3i + 0.006462602, r4i - 0.0997090
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.09205318, r2 + 0.12795857, r3i + 0.004945782, r4i + 0.1973791
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.12746607, r2 + 0.38861520, r3i + 0.032905104, r4i + 0.4064376
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.13913927, r2 + 0.71773578, r3i + 0.115418423, r4i + 0.7967752
                    ))
            )
        )
    );
  }

  /**
   * <b>- 3.15 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     158-177 s
   *   </li>
   *   <li>
   *     180-195 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_315() {
    double r1 = 139.1215;
    double r2 = 210.7849;
    double r3i = 91.62664;
    double r4i = 207.0252;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4414895, r2 + 0.3619946, r3i + 0.24314533, r4i + 0.3806781
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2787247, r2 + 0.2746867, r3i + 0.14475254, r4i + 0.2348993
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1428292, r2 + 0.1129040, r3i + 0.07565158, r4i + 0.1223369
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1745385, r2 - 0.1491691, r3i - 0.08185806, r4i - 0.1320355
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2881961, r2 - 0.2038542, r3i - 0.12168710, r4i - 0.1594455
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3692234, r2 - 0.2081198, r3i - 0.17373434, r4i - 0.2085835
                    ))
            )
        )
    );
  }
}
