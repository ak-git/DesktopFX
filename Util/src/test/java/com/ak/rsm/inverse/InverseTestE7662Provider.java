package com.ak.rsm.inverse;

import java.util.List;
import java.util.stream.Stream;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import static com.ak.rsm.measurement.Measurements.fixOhms;
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
    double r3i = 80.57511;
    double r4i = 167.6824;
    double smmBase = 6.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 2).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.0448797, r2 - 0.0498567, r3i + 0.02789828, r4i + 0.0005387763
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
    double r3i = 80.6423;
    double r4i = 168.5393;
    double smmBase = 6.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.105 * 2).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.04144541, r2 - 0.08550949, r3i - 0.001304607, r4i - 0.08518546
                    ))
            )
        )
    );
  }
}
