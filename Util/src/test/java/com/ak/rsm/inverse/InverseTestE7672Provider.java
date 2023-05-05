package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

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
    double r3 = 213.624;
    double r4 = 244.5108;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 6).system4(6.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3764922, r2 - 1.8356681, r3 - 0.974016, r4 - 2.6624756),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 5).system4(6.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3239197, r2 - 1.5323863, r3 - 0.7732634, r4 - 2.169864),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 4).system4(6.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2455330, r2 - 1.1724415, r3 - 0.6221236, r4 - 1.7340572),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 3).system4(6.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1790614, r2 - 0.9208401, r3 - 0.509913, r4 - 1.254868),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 2).system4(6.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1289355, r2 - 0.6114270, r3 - 0.316859, r4 - 0.8874894)
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
    double r3 = 208.4342;
    double r4 = 235.3128;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 7).system4(6.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.045192292, r2 - 0.6824200, r3 - 0.27374548, r4 - 0.98135052),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 5).system4(6.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.032585246, r2 - 0.4516261, r3 - 0.18424458, r4 - 0.70596922),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 3).system4(6.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.025239628, r2 - 0.2764949, r3 - 0.09686896, r4 - 0.38989824)
            )
        )
    );
  }
}
