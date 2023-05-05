package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

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
    double r3 = 153.1213;
    double r4 = 183.5731;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105 * 2).system4(6.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.0728419, r2 + 0.1269214, r3 - 0.0778365, r4 + 0.3418471)
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
    double r3 = 151.9314;
    double r4 = 178.5856;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105 * 2).system4(6.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.07827071, r2 + 0.05447255, r3 - 0.06070678, r4 + 0.17144244)
            )
        )
    );
  }
}
