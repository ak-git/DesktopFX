package com.ak.rsm.inverse;

import java.util.List;
import java.util.stream.Stream;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import static com.ak.rsm.measurement.Measurements.fixOhms;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7672Provider {
  private InverseTestE7672Provider() {
  }

  /**
   * <b>0.0 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     1-58 s
   *   </li>
   *   <li>
   *     59-96 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> ak16_44_53() {
    double r1 = 148.4767;
    double r2 = 221.7944;
    double r3i = 106.812;
    double r4i = 229.0674;
    double smmBase = 6.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 6).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3764922, r2 - 1.8356681, r3i - 0.4870080, r4i - 1.8182458
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 5).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3239197, r2 - 1.5323863, r3i - 0.3866317, r4i - 1.4715637
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 4).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2455330, r2 - 1.1724415, r3i - 0.3110618, r4i - 1.1780904
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 3).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1790614, r2 - 0.9208401, r3i - 0.2549565, r4i - 0.8823905
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 2).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1289355, r2 - 0.6114270, r3i - 0.1584295, r4i - 0.6021742
                    ))
            )
        )
    );
  }

  /**
   * <b>0.0 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     123-136 s
   *   </li>
   *   <li>
   *     141-168 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> ak16_44_53_3layers() {
    double r1 = 146.4665;
    double r2 = 216.3063;
    double r3i = 104.2171;
    double r4i = 221.8735;
    double smmBase = 6.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 7).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.045192292, r2 - 0.6824200, r3i - 0.13687274, r4i - 0.6275480
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 5).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.032585246, r2 - 0.4516261, r3i - 0.09212229, r4i - 0.4451069
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 3).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.025239628, r2 - 0.2764949, r3i - 0.04843448, r4i - 0.2433836
                    ))
            )
        )
    );
  }
}
