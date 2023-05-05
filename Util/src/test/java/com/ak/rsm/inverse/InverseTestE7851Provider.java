package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

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
    double r3 = 182.5668;
    double r4 = 233.2902;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.21813130, r2 - 0.40941236, r3 + 0.087925476, r4 - 0.887716076),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.15973889, r2 - 0.20821824, r3 + 0.01685278, r4 - 0.52231458),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.07675139, r2 - 0.08883005, r3 + 0.012925204, r4 - 0.212343204),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.09205318, r2 + 0.12795857, r3 + 0.009891564, r4 + 0.384866636),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.12746607, r2 + 0.38861520, r3 + 0.065810208, r4 + 0.747064992),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.13913927, r2 + 0.71773578, r3 + 0.230836846, r4 + 1.362713554)
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
    double r3 = 183.25328;
    double r4 = 230.79712;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4414895, r2 + 0.3619946, r3 + 0.48629066, r4 + 0.27506554),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2787247, r2 + 0.2746867, r3 + 0.28950508, r4 + 0.18029352),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1428292, r2 + 0.1129040, r3 + 0.15130316, r4 + 0.09337064),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1745385, r2 - 0.1491691, r3 - 0.16371612, r4 - -0.10035488),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2881961, r2 - 0.2038542, r3 - 0.2433742, r4 - 0.0755168),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3692234, r2 - 0.2081198, r3 - 0.34746868, r4 - 0.06969832)
            )
        )
    );
  }

  /**
   * <b>- 4.20 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     199-214 s
   *   </li>
   *   <li>
   *     217-232 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e7851_14_49_05_420() {
    double r1 = 139.8751;
    double r2 = 211.1441;
    double r3 = 184.06236;
    double r4 = 230.81384;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4266107, r2 + 0.4028756, r3 + 0.5095287, r4 + 0.3344965),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2555267, r2 + 0.2682906, r3 + 0.23323116, r4 + 0.25707624),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1520805, r2 + 0.1762088, r3 + 0.10973776, r4 + 0.09720084),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1531021, r2 - 0.1676564, r3 - 0.16745574, r4 - 0.17565266),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3078138, r2 - 0.3316960, r3 - 0.3151196, r4 - 0.2534834),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.4344795, r2 - 0.4605534, r3 - 0.39883204, r4 - 0.32137176)
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
    double r3 = 184.88714;
    double r4 = 230.96526;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4358391, r2 + 0.4759585, r3 + 0.57077258, r4 + 0.53221002),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2859043, r2 + 0.3187362, r3 + 0.30908228, r4 + 0.26271792),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1412331, r2 + 0.1597947, r3 + 0.17135848, r4 + 0.21158812),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1566764, r2 - 0.1737637, r3 - 0.17469198, r4 - 0.20169942),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2847417, r2 - 0.3200587, r3 - 0.36570088, r4 - 0.36151932),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.4542327, r2 - 0.5100261, r3 - 0.4755287, r4 - 0.3857405)
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
    double r3 = 185.73274;
    double r4 = 231.19206;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4837421, r2 + 0.4701067, r3 + 0.62519306, r4 + 0.53898954),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2946373, r2 + 0.3546993, r3 + 0.33746576, r4 + 0.32687704),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2000719, r2 + 0.2305505, r3 + 0.17528146, r4 + 0.16625614),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1630529, r2 - 0.1823617, r3 - 0.14252926, r4 - 0.13124734),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3457596, r2 - 0.3695723, r3 - 0.4364131, r4 - 0.3281163),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.4654294, r2 - 0.5629469, r3 - 0.51662334, r4 - 0.50172006)
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
    double r3 = 187.33736;
    double r4 = 231.92184;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4798688, r2 + 0.4692334, r3 + 0.58639736, r4 + 0.42751244),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3054350, r2 + 0.3452664, r3 + 0.35333926, r4 + 0.26011074),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1631314, r2 + 0.1394360, r3 + 0.21659028, r4 + 0.16635412),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1770010, r2 - 0.1845688, r3 - 0.18746418, r4 - 0.13197322),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3531438, r2 - 0.3859571, r3 - 0.34910318, r4 - 0.25186582),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.4744571, r2 - 0.5231157, r3 - 0.51390598, r4 - 0.41984682)
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
    double r3 = 188.61266;
    double r4 = 232.35974;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4845304, r2 + 0.5090087, r3 + 0.6770196, r4 + 0.4512788),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3248585, r2 + 0.3571460, r3 + 0.391045, r4 + 0.3134752),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1583871, r2 + 0.1611179, r3 + 0.229348, r4 + 0.1003188),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1812821, r2 - 0.2105244, r3 - 0.2360682, r4 - 0.2031594),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3683891, r2 - 0.3863174, r3 - 0.3930892, r4 - 0.2663194),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.4940038, r2 - 0.4970268, r3 - 0.4999466, r4 - 0.4523204)
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
    double r3 = 190.15578;
    double r4 = 232.95962;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3758237, r2 + 0.4935140, r3 + 0.48211776, r4 + 0.55881104),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2517574, r2 + 0.3364142, r3 + 0.34380044, r4 + 0.28459236),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1412483, r2 + 0.1885203, r3 + 0.14721718, r4 + 0.17827642),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1327389, r2 - 0.1512177, r3 - 0.21569956, r4 - 0.25004924),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2740245, r2 - 0.3627246, r3 - 0.43111822, r4 - 0.36844698),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.4343145, r2 - 0.5509377, r3 - 0.5932916, r4 - 0.430726)
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
    double r3 = 192.09296;
    double r4 = 234.60504;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3339903, r2 + 0.5098456, r3 + 0.4446895, r4 + 0.5405171),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1849729, r2 + 0.2777225, r3 + 0.2673425, r4 + 0.3260087),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1094900, r2 + 0.1554909, r3 + 0.14205178, r4 + 0.18414482),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1221675, r2 - 0.1785015, r3 - 0.16851908, r4 - 0.21582292),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2358737, r2 - 0.3257238, r3 - 0.31355134, r4 - 0.30672146),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3206202, r2 - 0.4328565, r3 - 0.45609364, r4 - 0.41807016)
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
    double r3 = 192.48146;
    double r4 = 237.10714;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3431112, r2 + 0.5295561, r3 + 0.4962418, r4 + 0.6237204),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1903040, r2 + 0.3035757, r3 + 0.31552824, r4 + 0.31469856),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1038409, r2 - 0.1484787, r3 - 0.21516024, r4 - 0.17485456),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2427463, r2 - 0.3923044, r3 - 0.33258662, r4 - 0.43639178),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3270014, r2 - 0.5200907, r3 - 0.48700744, r4 - 0.51599136)
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
    double r3 = 193.09882;
    double r4 = 237.45578;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3490481, r2 + 0.5230001, r3 + 0.41794196, r4 + 0.29497264),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2548873, r2 + 0.4228552, r3 + 0.254844, r4 + 0.1912804),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1445365, r2 + 0.1895408, r3 + 0.08164118, r4 + 0.17362862),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1141686, r2 - 0.1581170, r3 - 0.10436456, r4 - 0.13790644),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2143888, r2 - 0.3189721, r3 - 0.23119368, r4 - 0.31651292),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3101820, r2 - 0.4753185, r3 - 0.32184592, r4 - 0.38322688)
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
    double r3 = 184.43298;
    double r4 = 228.64822;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2779005, r2 + 0.3707453, r3 + 0.47475206, r4 + 0.42309754),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2213975, r2 + 0.2873218, r3 + 0.22037776, r4 + 0.27737152),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1294800, r2 + 0.1606253, r3 + 0.1211746, r4 + 0.19231582),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1080427, r2 - 0.1451910, r3 - 0.08905934, r4 - 0.07521074),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2344775, r2 - 0.2761995, r3 - 0.26464166, r4 - 0.2475052),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3301483, r2 - 0.3745818, r3 - 0.45289352, r4 - 0.44033978)
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
    double r3 = 186.77806;
    double r4 = 230.78754;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.32795788, r2 + 0.4027465, r3 + 0.36350476, r4 + 0.38130104),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.18239762, r2 + 0.2533714, r3 + 0.24517682, r4 + 0.20644394),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.10100006, r2 + 0.1248362, r3 + 0.10355976, r4 + 0.12434758),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.07855526, r2 - 0.1305952, r3 - 0.11666164, r4 - 0.07637766),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.18851840, r2 - 0.2386067, r3 - 0.1738692, r4 - 0.24979686),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.28622484, r2 - 0.3761246, r3 - 0.36719484, r4 - 0.43012952)
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
    double r3 = 184.69018;
    double r4 = 227.99882;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.19229927, r2 + 0.24300738, r3 + 0.2114806, r4 + 0.16495112),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.10417481, r2 + 0.10090665, r3 + 0.1527554, r4 + 0.0568904),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.03074559, r2 + 0.03604001, r3 + 0.06553786, r4 + 0.01698282),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.11097436, r2 - 0.14233093, r3 - 0.05412224, r4 - 0.09376764),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.10612553, r2 - 0.12121598, r3 - 0.10605616, r4 - 0.05252822),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.16486247, r2 - 0.14372020, r3 - 0.22538428, r4 - 0.06917028)
            )
        )
    );
  }
}
