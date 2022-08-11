package com.ak.rsm.inverse;

import java.util.List;
import java.util.stream.Stream;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import static com.ak.rsm.measurement.Measurements.fixOhms;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7851Provider {
  private InverseTestE7851Provider() {
  }

  /**
   * <b>- 1.05 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     62-85 s
   *   </li>
   *   <li>
   *     88-104 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_105() {
    double r1 = 137.9309;
    double r2 = 211.3749;
    double r3i = 91.2834;
    double r4i = 207.9285;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.21813130, r2 - 0.40941236, r3i + 0.043962738, r4i - 0.3998953
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.15973889, r2 - 0.20821824, r3i + 0.008426390, r4i - 0.2527309
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.07675139, r2 - 0.08883005, r3i + 0.006462602, r4i - 0.0997090
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.09205318, r2 + 0.12795857, r3i + 0.004945782, r4i + 0.1973791
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.12746607, r2 + 0.38861520, r3i + 0.032905104, r4i + 0.4064376
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.13913927, r2 + 0.71773578, r3i + 0.115418423, r4i + 0.7967752
                    ))
            )
        )
    );
  }

  /**
   * <b>- 3.15 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     158-177 s
   *   </li>
   *   <li>
   *     180-195 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_315() {
    double r1 = 139.1215;
    double r2 = 210.7849;
    double r3i = 91.62664;
    double r4i = 207.0252;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4414895, r2 + 0.3619946, r3i + 0.24314533, r4i + 0.3806781
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2787247, r2 + 0.2746867, r3i + 0.14475254, r4i + 0.2348993
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1428292, r2 + 0.1129040, r3i + 0.07565158, r4i + 0.1223369
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1745385, r2 - 0.1491691, r3i - 0.08185806, r4i - 0.1320355
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2881961, r2 - 0.2038542, r3i - 0.12168710, r4i - 0.1594455
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3692234, r2 - 0.2081198, r3i - 0.17373434, r4i - 0.2085835
                    ))
            )
        )
    );
  }

  /**
   * <b>- 4.20 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     199-215 s
   *   </li>
   *   <li>
   *     217-232 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_420() {
    double r1 = 139.8676;
    double r2 = 211.1286;
    double r3i = 92.03118;
    double r4i = 207.4381;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4266107, r2 + 0.4028756, r3i + 0.25476435, r4i + 0.4220126
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2555267, r2 + 0.2682906, r3i + 0.11661558, r4i + 0.2451537
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1520805, r2 + 0.1762088, r3i + 0.05486888, r4i + 0.1034693
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1531021, r2 - 0.1676564, r3i - 0.08372787, r4i - 0.1715542
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3078138, r2 - 0.3316960, r3i - 0.15755980, r4i - 0.2843015
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.4344795, r2 - 0.4605534, r3i - 0.19941602, r4i - 0.3601019
                    ))
            )
        )
    );
  }

  /**
   * <b>- 5.25 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     236-251 s
   *   </li>
   *   <li>
   *     254-269 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_525() {
    double r1 = 140.6911;
    double r2 = 211.4494;
    double r3i = 92.44357;
    double r4i = 207.9262;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4358391, r2 + 0.4759585, r3i + 0.28538629, r4i + 0.5514913
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2859043, r2 + 0.3187362, r3i + 0.15454114, r4i + 0.2859001
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1412331, r2 + 0.1597947, r3i + 0.08567924, r4i + 0.1914733
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1566764, r2 - 0.1737637, r3i - 0.08734599, r4i - 0.1881957
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2847417, r2 - 0.3200587, r3i - 0.18285044, r4i - 0.3636101
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.4542327, r2 - 0.5100261, r3i - 0.23776435, r4i - 0.4306346
                    ))
            )
        )
    );
  }

  /**
   * <b>- 6.09 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     274-287 s
   *   </li>
   *   <li>
   *     288-307 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_609() {
    double r1 = 141.3052;
    double r2 = 211.9742;
    double r3i = 92.86637;
    double r4i = 208.4624;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4837421, r2 + 0.4701067, r3i + 0.31259653, r4i + 0.5820913
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2946373, r2 + 0.3546993, r3i + 0.16873288, r4i + 0.3321714
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2000719, r2 + 0.2305505, r3i + 0.08764073, r4i + 0.1707688
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1630529, r2 - 0.1823617, r3i - 0.07126463, r4i - 0.1368883
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3457596, r2 - 0.3695723, r3i - 0.21820655, r4i - 0.3822647
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.4654294, r2 - 0.5629469, r3i - 0.25831167, r4i - 0.5091717
                    ))
            )
        )
    );
  }

  /**
   * <b>- 7.35 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     310-323 s
   *   </li>
   *   <li>
   *     324-343 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_735() {
    double r1 = 142.5128;
    double r2 = 213.1574;
    double r3i = 93.66868;
    double r4i = 209.6296;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4798688, r2 + 0.4692334, r3i + 0.29319868, r4i + 0.5069549
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3054350, r2 + 0.3452664, r3i + 0.17666963, r4i + 0.3067250
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1631314, r2 + 0.1394360, r3i + 0.10829514, r4i + 0.1914722
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1770010, r2 - 0.1845688, r3i - 0.09373209, r4i - 0.1597187
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3531438, r2 - 0.3859571, r3i - 0.17455159, r4i - 0.3004845
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.4744571, r2 - 0.5231157, r3i - 0.25695299, r4i - 0.4668764
                    ))
            )
        )
    );
  }

  /**
   * <b>- 8.40 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     351-364 s
   *   </li>
   *   <li>
   *     367-380 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_840() {
    double r1 = 143.4136;
    double r2 = 213.9471;
    double r3i = 94.30633;
    double r4i = 210.4862;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4845304, r2 + 0.5090087, r3i + 0.3385098, r4i + 0.5641492
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3248585, r2 + 0.3571460, r3i + 0.1955225, r4i + 0.3522601
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1583871, r2 + 0.1611179, r3i + 0.1146740, r4i + 0.1648334
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1812821, r2 - 0.2105244, r3i - 0.1180341, r4i - 0.2196138
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3683891, r2 - 0.3863174, r3i - 0.1965446, r4i - 0.3297043
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.4940038, r2 - 0.4970268, r3i - 0.2499733, r4i - 0.4761335
                    ))
            )
        )
    );
  }

  /**
   * <b>- 9.45 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     384-397 s
   *   </li>
   *   <li>
   *     400-415 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_945() {
    double r1 = 144.3973;
    double r2 = 214.9162;
    double r3i = 95.07789;
    double r4i = 211.5577;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3758237, r2 + 0.4935140, r3i + 0.24105888, r4i + 0.5204644
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2517574, r2 + 0.3364142, r3i + 0.17190022, r4i + 0.3141964
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1412483, r2 + 0.1885203, r3i + 0.07360859, r4i + 0.1627468
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1327389, r2 - 0.1512177, r3i - 0.10784978, r4i - 0.2328744
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2740245, r2 - 0.3627246, r3i - 0.21555911, r4i - 0.3997826
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.4343145, r2 - 0.5509377, r3i - 0.29664580, r4i - 0.5120088
                    ))
            )
        )
    );
  }
}
