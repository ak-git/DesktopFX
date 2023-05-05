package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7645Provider {
  private InverseTestE7645Provider() {
  }

  /**
   * <b>0.0 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     0.5-16 s
   *   </li>
   *   <li>
   *     20.5-28 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> n18_09_44() {
    double r1 = 164.0202;
    double r2 = 239.5422;
    double r3 = 212.5948;
    double r4 = 263.8774;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 2).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.09589374, r2 + 0.1857942, r3 + 0.18911808, r4 + 0.14903632)
            )
        )
    );
  }

  /**
   * <b>0.0 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     4.5-14 s
   *   </li>
   *   <li>
   *     18.5-26 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> n18_10_42() {
    double r1 = 164.9797;
    double r2 = 240.6624;
    double r3 = 212.9514;
    double r4 = 263.4848;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 2).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.0593253, r2 + 0.0954033, r3 + 0.07798168, r4 + 0.0349112)
            )
        )
    );
  }

  /**
   * <b>0.0 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     0.5-14 s
   *   </li>
   *   <li>
   *     15.5-32 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> n18_12_22() {
    double r1 = 165.9119;
    double r2 = 249.7204;
    double r3 = 222.815;
    double r4 = 284.256;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 2).system4(10.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1805104, r2 - 0.6408006, r3 - 0.4291292, r4 - 0.8318322)
            )
        )
    );
  }

  /**
   * <b>0.0 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     0.5-14 s
   *   </li>
   * </ul>
   *
   * @return s2
   */
  static Stream<Arguments> n18_12_59() {
    double r1 = 166.1417;
    double r2 = 250.6794;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105 * 2).system2(10.0)
                    .ofOhms(r1, r2, r1 + 0.1511312, r2 + 0.5706979)
            )
        )
    );
  }
}
