package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7661Provider {
  private InverseTestE7661Provider() {
  }

  /**
   * <b>0.0 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     1-9 s
   *   </li>
   *   <li>
   *     11-26 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> ak14_26_39() {
    double r1 = 96.9555;
    double r2 = 142.482;
    double r3 = 123.24632;
    double r4 = 154.21808;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1498728, r2 + 0.4866054, r3 + 0.2643216, r4 + 0.5918692)
            )
        )
    );
  }

  /**
   * <b>0.0 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     9-22 s
   *   </li>
   *   <li>
   *     11-26 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> ak14_27_16layer3() {
    double r1 = 92.58523;
    double r2 = 133.918;
    double r3 = 113.86074;
    double r4 = 147.59486;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105 * 2).system4(7.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.007848626, r2 + 0.1189516, r3 + 0.08199872, r4 + 0.16500188)
            )
        )
    );
  }
}
