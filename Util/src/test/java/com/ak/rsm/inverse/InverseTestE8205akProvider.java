package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE8205akProvider {
  private InverseTestE8205akProvider() {
  }

  /**
   * <b>2023-02-16 18-11-27</b>
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
   *   <li>
   *     0*
   *   </li>
   *   <li>
   *     +30*
   *   </li>
   *   <li>
   *     +45*
   *   </li>
   * </ol>
   *
   * @return s2
   */
  static Stream<Arguments> e8205_18_11_27() {
    double smmBase = 7.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(131.5445, 204.6389, 131.5445 + 0.3086365, 204.6389 + 0.8528327),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(131.5445, 204.6389, 131.5445 + 0.7066925, 204.6389 + 1.9498802)
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(134.5504, 211.7489, 134.5504 + 0.5928728, 211.7489 + 1.248164),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(134.5504, 211.7489, 134.5504 + 1.4630382, 211.7489 + 3.348061)
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(135.954, 215.9569, 135.954 + 0.6731754, 215.9569 + 1.338014),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(135.954, 215.9569, 135.954 + 1.4590649, 215.9569 + 2.907502)
            )
        ),

        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(139.3494, 222.5109, 139.3494 + 0.7615459, 222.5109 + 1.475829),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(139.3494, 222.5109, 139.3494 + 1.8368335, 222.5109 + 3.439238)
            )
        ),

        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(135.1871, 216.978, 135.1871 + 0.5577181, 216.978 + 1.308204),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(135.1871, 216.978, 135.1871 + 1.2827309, 216.978 + 2.749946)
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(132.6675, 211.6793, 132.6675 + 0.4490636, 211.6793 + 1.081955),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(132.6675, 211.6793, 132.6675 + 1.0256711, 211.6793 + 2.432371)
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(130.1996, 201.492, 130.1996 + 0.1602974, 201.492 + 0.6569142),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(130.1996, 201.492, 130.1996 + 0.4395959, 201.492 + 1.3911093)
            )
        )
    );
  }

  /**
   * <b>2023-02-16 18-06-48 </b>
   * <ol>
   *   <li>
   *     0 Н
   *   </li>
   *   <li>
   *     17 Н
   *   </li>
   *   <li>
   *     30 Н
   *   </li>
   *   <li>
   *     48 Н
   *   </li>
   *   <li>
   *     28 Н
   *   </li>
   *   <li>
   *     15 Н
   *   </li>
   *   <li>
   *     2 Н
   *   </li>
   * </ol>
   *
   * @return s2
   */
  static Stream<Arguments> e8205_18_06_48() {
    double smmBase = 7.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(131.6894, 206.2727, 131.6894 + 0.06014545, 206.2727 + 0.3877851),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(131.6894, 206.2727, 131.6894 + 0.14890040, 206.2727 + 0.8069476)
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(131.7947, 206.2855, 131.7947 + 0.04366012, 206.2855 + 0.3702430),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(131.7947, 206.2855, 131.7947 + 0.10668236, 206.2855 + 0.7721687)
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(131.3457, 205.5573, 131.3457 + 0.04013099, 205.5573 + 0.3753909),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(131.3457, 205.5573, 131.3457 + 0.06456883, 205.5573 + 0.7686446)
            )
        ),

        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(130.1744, 204.1539, 130.1744 + 0.006601419, 204.1539 + 0.3240038),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(130.1744, 204.1539, 130.1744 + 0.006601419 * 2, 204.1539 + 0.6915894)
            )
        ),

        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(129.5755, 203.6841, 129.5755 + 0.01631487, 203.6841 + 0.3290539)
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(129.7782, 203.8138, 129.7782 + 0.02565866, 203.8138 + 0.4015964),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210 * 2).system2(smmBase)
                    .ofOhms(129.7782, 203.8138, 129.7782 + 0.09211332, 203.8138 + 0.8545673)
            )
        ),
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.210).system2(smmBase)
                    .ofOhms(129.8102, 203.5274, 129.8102 + 0.03619336, 203.5274 + 0.4043668)
            )
        )
    );
  }
}
