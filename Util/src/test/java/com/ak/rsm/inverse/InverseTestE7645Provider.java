package com.ak.rsm.inverse;

import java.util.List;
import java.util.stream.Stream;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import static com.ak.rsm.measurement.Measurements.fixOhms;
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
    double r3i = 106.2974;
    double r4i = 238.2361;
    double smmBase = 10.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 2).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.09589374, r2 + 0.1857942, r3i + 0.09455904, r4i + 0.1690772
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
    double r3i = 106.4757;
    double r4i = 238.2181;
    double smmBase = 10.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 2).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.0593253, r2 + 0.0954033, r3i + 0.03899084, r4i + 0.05644644
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
    double r3i = 111.4075;
    double r4i = 253.5355;
    double smmBase = 10.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 2).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1805104, r2 - 0.6408006, r3i - 0.2145646, r4i - 0.6304807
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
   *     0.5-14 s
   *   </li>
   * </ul>
   *
   * @return s2
   */
  static Stream<Arguments> n18_12_59() {
    double r1 = 166.1417;
    double r2 = 250.6794;
    double smmBase = 10.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105 * 2).system2(smmBase)
                    .ofOhms(r1, r2, r1 + 0.1511312, r2 + 0.5706979)
            )
        )
    );
  }
}
