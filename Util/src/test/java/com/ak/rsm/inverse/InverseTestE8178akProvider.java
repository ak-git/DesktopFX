package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE8178akProvider {
  private InverseTestE8178akProvider() {
  }

  /**
   * <b>2023-02-02 17-45-08</b>
   * <ol>
   *   <li>
   *     +45*
   *   </li>
   *   <li>
   *     +30*
   *   </li>
   *   <li>
   *     0*
   *   </li>
   *   <li>
   *     -30*
   *   </li>
   * </ol>
   *
   * @return s2
   */
  static Stream<Arguments> e8178_17_45_08() {
    double smmBase = 7.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(141.9151, 212.1288, 141.9151 + 0.1535285, 212.1288 + 0.8035406),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(141.9151, 212.1288, 141.9151 + 0.4672581, 212.1288 + 1.7376076)
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(143.4547, 215.6121, 143.4547 + 0.2791814, 215.6121 + 0.9028086),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(143.4547, 215.6121, 143.4547 + 0.7585438, 215.6121 + 1.9349331)
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(145.5582, 218.6326, 145.5582 + 0.4132879, 218.6326 + 1.152833),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(145.5582, 218.6326, 145.5582 + 0.9250459, 218.6326 + 2.474967)
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(147.4425, 219.1882, 147.4425 + 0.4891372, 219.1882 + 1.122328),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(147.4425, 219.1882, 147.4425 + 1.0575266, 219.1882 + 2.495164)
            )
        )
    );
  }
}
