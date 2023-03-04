package com.ak.rsm.inverse;

import java.util.List;
import java.util.stream.Stream;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import static com.ak.rsm.measurement.Measurements.fixOhms;
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
    double r3i = 61.62316;
    double r4i = 138.7322;
    double smmBase = 7.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105 * 2).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1498728, r2 + 0.4866054, r3i + 0.1321608, r4i + 0.4280954
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
    double r3i = 56.93037;
    double r4i = 130.7278;
    double smmBase = 7.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105 * 2).system4(smmBase)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.007848626, r2 + 0.1189516, r3i + 0.04099936, r4i + 0.1235003
                    ))
            )
        )
    );
  }
}
