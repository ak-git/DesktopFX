package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseTestE7858Provider {
  private InverseTestE7858Provider() {
  }

  /**
   * <b>- 1.05 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     36-53 s
   *   </li>
   *   <li>
   *     56-69 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_105a() {
    double r1 = 168.2099;
    double r2 = 259.3473;
    double r3 = 225.5754;
    double r4 = 307.0014;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3723485, r2 + 0.5232728, r3 + 0.49580532, r4 + 0.54156908),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2378502, r2 + 0.3060483, r3 + 0.30124744, r4 + 0.32161676),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1042702, r2 + 0.1563920, r3 + 0.16418672, r4 + 0.13883748),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.0993205, r2 - 0.1504483, r3 - 0.158073, r4 - 0.1307856),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2205720, r2 - 0.2759144, r3 - 0.26809824, r4 - 0.19083636),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3572805, r2 - 0.3816782, r3 - 0.37970816, r4 - 0.23509164)
            )
        )
    );
  }

  /**
   * <b>- 2.10 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     75-92 s
   *   </li>
   *   <li>
   *     95-108 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_210a() {
    double r1 = 168.5869;
    double r2 = 259.5104;
    double r3 = 225.3468;
    double r4 = 307.1328;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3528490, r2 + 0.5676400, r3 + 0.713796, r4 + 0.6236088),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2499231, r2 + 0.4064239, r3 + 0.4492458, r4 + 0.3985872),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1363070, r2 + 0.2172058, r3 + 0.2227534, r4 + 0.140875),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1481242, r2 - 0.2275767, r3 - 0.2076366, r4 - 0.283942),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2789294, r2 - 0.4110700, r3 - 0.417024, r4 - 0.4102544),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3589339, r2 - 0.5313245, r3 - 0.5089448, r4 - 0.4660844)
            )
        )
    );
  }

  /**
   * <b>- 3.15 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     112-129 s
   *   </li>
   *   <li>
   *     130-145 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_315a() {
    double r1 = 169.1048;
    double r2 = 260.3135;
    double r3 = 226.776;
    double r4 = 306.8926;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4736198, r2 + 0.7246873, r3 + 0.7992368, r4 + 0.848031),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3405742, r2 + 0.4706920, r3 + 0.5260152, r4 + 0.4816408),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2017308, r2 + 0.2929059, r3 + 0.2547378, r4 + 0.229062),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2022860, r2 - 0.3073639, r3 - 0.2987714, r4 - 0.3287442),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3158145, r2 - 0.4853530, r3 - 0.5323506, r4 - 0.5489786),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.4589221, r2 - 0.6933638, r3 - 0.686416, r4 - 0.7116964)
            )
        )
    );
  }

  /**
   * <b>- 4.20 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     151-168 s
   *   </li>
   *   <li>
   *     130-145 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_420a() {
    double r1 = 170.5601;
    double r2 = 261.429;
    double r3 = 228.9634;
    double r4 = 307.1452;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.5659504, r2 + 0.8062122, r3 + 0.8662724, r4 + 0.9360884),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3607682, r2 + 0.5221096, r3 + 0.5304944, r4 + 0.4954016),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1957776, r2 + 0.2977777, r3 + 0.2869122, r4 + 0.2200424),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2358813, r2 - 0.3368890, r3 - 0.3577248, r4 - 0.316359),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3983539, r2 - 0.5668518, r3 - 0.5866264, r4 - 0.5498056),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.5301981, r2 - 0.7722706, r3 - 0.838248, r4 - 0.8086274)
            )
        )
    );
  }

  /**
   * <b>- 5.25 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     192-207 s
   *   </li>
   *   <li>
   *     208-223 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_525a() {
    double r1 = 172.3937;
    double r2 = 262.7724;
    double r3 = 231.573;
    double r4 = 307.2378;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.7350223, r2 + 0.8762090, r3 + 0.9555564, r4 + 0.781352),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3846955, r2 + 0.4891318, r3 + 0.5703194, r4 + 0.4099744),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2031664, r2 + 0.2626881, r3 + 0.2555914, r4 + 0.2094206),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1774332, r2 - 0.2974556, r3 - 0.3654668, r4 - 0.20061),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.4326267, r2 - 0.5816926, r3 - 0.6067242, r4 - 0.522257),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.5808185, r2 - 0.7540085, r3 - 0.8331842, r4 - 0.6852388)
            )
        )
    );
  }

  /**
   * <b>- 6.30 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     229-246 s
   *   </li>
   *   <li>
   *     248-264 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_630a() {
    double r1 = 174.7324;
    double r2 = 265.0763;
    double r3 = 234.1988;
    double r4 = 308.3504;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.7048978, r2 + 0.9405241, r3 + 0.9562834, r4 + 0.8357482),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3255025, r2 + 0.5036681, r3 + 0.4672052, r4 + 0.4786244),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2196114, r2 + 0.2946281, r3 + 0.2945374, r4 + 0.1977464),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2184635, r2 - 0.3073761, r3 - 0.3243982, r4 - 0.268239),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.4450089, r2 - 0.5720805, r3 - 0.6814462, r4 - 0.4481968),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.5857961, r2 - 0.7534573, r3 - 0.9228396, r4 - 0.663991)
            )
        )
    );
  }

  /**
   * <b>- 7.35 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     268-283 s
   *   </li>
   *   <li>
   *     284-301 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_735() {
    double r1 = 176.0088;
    double r2 = 267.9422;
    double r3 = 234.7542;
    double r4 = 315.2678;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.5808250, r2 + 1.0033374, r3 + 0.932381, r4 + 1.069199),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3482697, r2 + 0.4832408, r3 + 0.4633976, r4 + 0.704841),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1786155, r2 + 0.2974441, r3 + 0.2839348, r4 + 0.234392),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2097833, r2 - 0.3400561, r3 - 0.3602996, r4 - 0.2839196),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3222572, r2 - 0.7079429, r3 - 0.5919428, r4 - 0.621305),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.5164597, r2 - 0.8535159, r3 - 0.8250492, r4 - 0.7496908)
            )
        )
    );
  }

  /**
   * <b>- 6.30 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     311-324 s
   *   </li>
   *   <li>
   *     327-340 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_630() {
    double r1 = 173.0028;
    double r2 = 266.6782;
    double r3 = 230.9414;
    double r4 = 313.6522;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.5911788, r2 + 0.7754185, r3 + 0.9568878, r4 + 0.9983792),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4072341, r2 + 0.5444370, r3 + 0.4629024, r4 + 0.453585),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1888553, r2 + 0.2670350, r3 + 0.217732, r4 + 0.234548),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2278112, r2 - 0.3546353, r3 - 0.3512212, r4 - 0.3532454),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3912851, r2 - 0.5829698, r3 - 0.5692568, r4 - 0.7670612),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.5983863, r2 - 0.8593577, r3 - 0.789944, r4 - 0.8760462)
            )
        )
    );
  }

  /**
   * <b>- 5.25 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     344-359 s
   *   </li>
   *   <li>
   *     360-379 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_525() {
    double r1 = 170.7367;
    double r2 = 263.8238;
    double r3 = 228.0972;
    double r4 = 309.9732;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.5988948, r2 + 0.8778407, r3 + 0.8437192, r4 + 0.9374186),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3537385, r2 + 0.5369471, r3 + 0.466036, r4 + 0.5511104),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1808150, r2 + 0.2610289, r3 + 0.2545992, r4 + 0.2673988),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2075135, r2 - 0.3358505, r3 - 0.2649794, r4 - 0.390862),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3965947, r2 - 0.5600700, r3 - 0.5393158, r4 - 0.6058234),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.5151627, r2 - 0.7666777, r3 - 0.769541, r4 - 0.7647264)
            )
        )
    );
  }

  /**
   * <b>- 4.20 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     383-400 s
   *   </li>
   *   <li>
   *     401-418 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_420() {
    double r1 = 169.1415;
    double r2 = 261.6261;
    double r3 = 226.2724;
    double r4 = 307.9318;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.4932189, r2 + 0.7325382, r3 + 0.7253902, r4 + 0.7428764),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2623814, r2 + 0.3852768, r3 + 0.4582526, r4 + 0.4100326),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1417828, r2 + 0.2033706, r3 + 0.2341886, r4 + 0.226852),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1503256, r2 - 0.2555846, r3 - 0.2942736, r4 - 0.261446),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3478208, r2 - 0.5186373, r3 - 0.490557, r4 - 0.4624352),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.4372682, r2 - 0.6724585, r3 - 0.5440832, r4 - 0.64857)
            )
        )
    );
  }

  /**
   * <b>- 3.15 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     422-437 s
   *   </li>
   *   <li>
   *     440-457 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_315() {
    double r1 = 168.0252;
    double r2 = 259.5638;
    double r3 = 224.7778;
    double r4 = 303.4632;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.3819845, r2 + 0.5967298, r3 + 0.598865, r4 + 0.7271218),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.2653264, r2 + 0.3758375, r3 + 0.381311, r4 + 0.386581),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.1395189, r2 + 0.1703012, r3 + 0.1945812, r4 + 0.2523086),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.1709272, r2 - 0.2402450, r3 - 0.235982, r4 - 0.2252888),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.2710274, r2 - 0.3760844, r3 - 0.4181558, r4 - 0.3820324),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.3458873, r2 - 0.5330884, r3 - 0.5213556, r4 - 0.4626318)
            )
        )
    );
  }

  /**
   * <b>- 2.10 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     461-480 s
   *   </li>
   *   <li>
   *     481-498 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_210() {
    double r1 = 167.9044;
    double r2 = 257.0107;
    double r3 = 223.607;
    double r4 = 302.0856;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.35754724, r2 + 0.4671662, r3 + 0.48897578, r4 + 0.48325862),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.22453965, r2 + 0.2769617, r3 + 0.32077054, r4 + 0.31367286),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.07846058, r2 + 0.1505218, r3 + 0.15995258, r4 + 0.19959602),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.11116876, r2 - 0.1679319, r3 - 0.13551948, r4 - 0.26443552),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.24125736, r2 - 0.2727228, r3 - 0.29121148, r4 - 0.19287312),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.33289038, r2 - 0.3423280, r3 - 0.42830002, r4 - 0.23334458)
            )
        )
    );
  }

  /**
   * <b>- 1.05 mm</b>
   * <br/>
   * <ul>
   *   <li>
   *     502-515 s
   *   </li>
   *   <li>
   *     520-535 s
   *   </li>
   * </ul>
   *
   * @return s4
   */
  static Stream<Arguments> e09_46_50_105() {
    double r1 = 167.0104;
    double r2 = 256.4222;
    double r3 = 222.5884;
    double r4 = 301.5806;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.25064381, r2 + 0.14096480, r3 + 0.30478412, r4 - 0.06588092),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.20491504, r2 + 0.13028252, r3 + 0.13521926, r4 + 0.00472016),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 + 0.05888929, r2 + 0.06870984, r3 + 0.04939712, r4 + 0.1014148),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.13626734, r2 - 0.10934137, r3 - 0.09492426, r4 - 0.02112406),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.17772637, r2 - 0.09705815, r3 - 0.12113302, r4 + 0.06092072),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 3).system4(8.0)
                    .ofOhms(r1, r2, r3, r4,
                        r1 - 0.33563704, r2 - 0.07558674, r3 - 0.4638456, r4 + 0.20820972)
            )
        )
    );
  }
}
