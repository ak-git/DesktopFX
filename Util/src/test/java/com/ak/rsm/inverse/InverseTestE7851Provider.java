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

  /**
   * <b>- 10.92 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     418-431 s
   *   </li>
   *   <li>
   *     434-449 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_1092() {
    double r1 = 145.7165;
    double r2 = 216.5495;
    double r3i = 96.04648;
    double r4i = 213.349;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3339903, r2 + 0.5098456, r3i + 0.22234475, r4i + 0.4926033
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1849729, r2 + 0.2777225, r3i + 0.13367125, r4i + 0.2966756
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1094900, r2 + 0.1554909, r3i + 0.07102589, r4i + 0.1630983
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1221675, r2 - 0.1785015, r3i - 0.08425954, r4i - 0.1921710
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2358737, r2 - 0.3257238, r3i - 0.15677567, r4i - 0.3101364
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3206202, r2 - 0.4328565, r3i - 0.22804682, r4i - 0.4370819
                    ))
            )
        )
    );
  }

  /**
   * <b>- 11.97 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     455-466 s
   *   </li>
   *   <li>
   *     467-482 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_1197() {
    double r1 = 146.1887;
    double r2 = 218.1336;
    double r3i = 96.24073;
    double r4i = 214.7943;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3431112, r2 + 0.5295561, r3i + 0.24812090, r4i + 0.5599811
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1903040, r2 + 0.3035757, r3i + 0.15776412, r4i + 0.3151134
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1038409, r2 - 0.1484787, r3i - 0.10758012, r4i - 0.1950074
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2427463, r2 - 0.3923044, r3i - 0.16629331, r4i - 0.3844892
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3270014, r2 - 0.5200907, r3i - 0.24350372, r4i - 0.5014994
                    ))
            )
        )
    );
  }

  /**
   * <b>- 11.97 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     483-512 s
   *   </li>
   *   <li>
   *     513-528 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_1197a() {
    double r1 = 146.5283;
    double r2 = 218.6496;
    double r3i = 96.54941;
    double r4i = 215.2773;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3490481, r2 + 0.5230001, r3i + 0.20897098, r4i + 0.3564573
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2548873, r2 + 0.4228552, r3i + 0.12742200, r4i + 0.2230622
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1445365, r2 + 0.1895408, r3i + 0.04082059, r4i + 0.1276349
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1141686, r2 - 0.1581170, r3i - 0.05218228, r4i - 0.1211355
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2143888, r2 - 0.3189721, r3i - 0.11559684, r4i - 0.2738533
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3101820, r2 - 0.4753185, r3i - 0.16092296, r4i - 0.3525364
                    ))
            )
        )
    );
  }

  /**
   * <b>- 5.67 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     537-550 s
   *   </li>
   *   <li>
   *     551-566 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_567() {
    double r1 = 141.0099;
    double r2 = 211.0433;
    double r3i = 92.21649;
    double r4i = 206.5406;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2779005, r2 + 0.3707453, r3i + 0.23737603, r4i + 0.44892480
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2213975, r2 + 0.2873218, r3i + 0.11018888, r4i + 0.24887464
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1294800, r2 + 0.1606253, r3i + 0.06058730, r4i + 0.15674521
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1080427, r2 - 0.1451910, r3i - 0.04452967, r4i - 0.08213504
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2344775, r2 - 0.2761995, r3i - 0.13232083, r4i - 0.25607343
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3301483, r2 - 0.3745818, r3i - 0.22644676, r4i - 0.44661665
                    ))
            )
        )
    );
  }

  /**
   * <b>- 5.67 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     569-582 s
   *   </li>
   *   <li>
   *     585-598 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_567a() {
    double r1 = 141.8003;
    double r2 = 212.2652;
    double r3i = 93.38903;
    double r4i = 208.7828;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.32795788, r2 + 0.4027465, r3i + 0.18175238, r4i + 0.37240290
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.18239762, r2 + 0.2533714, r3i + 0.12258841, r4i + 0.22581038
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.10100006, r2 + 0.1248362, r3i + 0.05177988, r4i + 0.11395367
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.07855526, r2 - 0.1305952, r3i - 0.05833082, r4i - 0.09651965
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.18851840, r2 - 0.2386067, r3i - 0.08693460, r4i - 0.21183303
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.28622484, r2 - 0.3761246, r3i - 0.18359742, r4i - 0.39866218
                    ))
            )
        )
    );
  }

  /**
   * <b>- 1.68 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     624-637 s
   *   </li>
   *   <li>
   *     640-653 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_168() {
    double r1 = 140.0501;
    double r2 = 209.6679;
    double r3i = 92.34509;
    double r4i = 206.3445;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.19229927, r2 + 0.24300738, r3i + 0.10574030, r4i + 0.18821586
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.10417481, r2 + 0.10090665, r3i + 0.07637770, r4i + 0.10482290
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.03074559, r2 + 0.03604001, r3i + 0.03276893, r4i + 0.04126034
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.11097436, r2 - 0.14233093, r3i - 0.02706112, r4i - 0.07394494
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.10612553, r2 - 0.12121598, r3i - 0.05302808, r4i - 0.07929219
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.16486247, r2 - 0.14372020, r3i - 0.11269214, r4i - 0.14727728
                    ))
            )
        )
    );
  }
}
