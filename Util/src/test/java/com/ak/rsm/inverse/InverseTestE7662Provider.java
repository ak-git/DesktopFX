package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7662Provider {
  private InverseTestE7662Provider() {
  }

  /**
   * <b>0.0 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     51-64 s
   *   </li>
   *   <li>
   *     121-140 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> ak20_50_03layer3() {
    double r1 = 124.0248;
    double r2 = 171.5511;
    double r3 = 161.15022;
    double r4 = 174.21458;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 2).system4(6.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.0448797, r2 - 0.0498567, r3 + 0.05579656, r4 - 0.0547190074)
            )
        )
    );
  }

  /**
   * <b>0.0 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     11-20 s
   *   </li>
   *   <li>
   *     121-140 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> ak20_53_41layer3() {
    double r1 = 123.7377;
    double r2 = 172.8966;
    double r3 = 161.2846;
    double r4 = 175.794;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 2).system4(6.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.04144541, r2 - 0.08550949, r3 - 0.002609214, r4 - 0.167761706)
            )
        )
    );
  }
}
