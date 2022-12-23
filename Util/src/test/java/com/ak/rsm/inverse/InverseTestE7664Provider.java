package com.ak.rsm.inverse;

import java.util.List;
import java.util.stream.Stream;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import static com.ak.rsm.measurement.Measurements.fixOhms;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7664Provider {
  private InverseTestE7664Provider() {
  }

  /**
   * <b>0.0 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     15-30 s
   *   </li>
   *   <li>
   *     41-50 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> ak15_54_22layer3() {
    double r1 = 127.1341;
    double r2 = 169.8944;
    double r3i = 76.56065;
    double r4i = 168.3472;
    double smmBase = 6.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105 * 2).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.0728419, r2 + 0.1269214, r3i - 0.03891825, r4i + 0.1320053
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
   *     7-19 s
   *   </li>
   *   <li>
   *     41-50 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> ak15_55_15layer3() {
    double r1 = 126.4524;
    double r2 = 167.3828;
    double r3i = 75.9657;
    double r4i = 165.2585;
    double smmBase = 6.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105 * 2).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.07827071, r2 + 0.05447255, r3i - 0.03035339, r4i + 0.05536783
                    ))
            )
        )
    );
  }
}
