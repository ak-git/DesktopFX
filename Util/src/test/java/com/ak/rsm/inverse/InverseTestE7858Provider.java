package com.ak.rsm.inverse;

import java.util.List;
import java.util.stream.Stream;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import org.junit.jupiter.params.provider.Arguments;

import static com.ak.rsm.measurement.Measurements.fixOhms;
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
    double r3i = 112.7877;
    double r4i = 266.2884;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3723485, r2 + 0.5232728, r3i + 0.24790266, r4i + 0.5186872
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2378502, r2 + 0.3060483, r3i + 0.15062372, r4i + 0.3114321
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1042702, r2 + 0.1563920, r3i + 0.08209336, r4i + 0.1515121
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.0993205, r2 - 0.1504483, r3i - 0.07903650, r4i - 0.1444293
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2205720, r2 - 0.2759144, r3i - 0.13404912, r4i - 0.2294673
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3572805, r2 - 0.3816782, r3i - 0.18985408, r4i - 0.3073999
                    ))
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
    double r3i = 112.6734;
    double r4i = 266.2398;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3528490, r2 + 0.5676400, r3i + 0.3568980, r4i + 0.6687024
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2499231, r2 + 0.4064239, r3i + 0.2246229, r4i + 0.4239165
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1363070, r2 + 0.2172058, r3i + 0.1113767, r4i + 0.1818142
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1481242, r2 - 0.2275767, r3i - 0.1038183, r4i - 0.2457893
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2789294, r2 - 0.4110700, r3i - 0.2085120, r4i - 0.4136392
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3589339, r2 - 0.5313245, r3i - 0.2544724, r4i - 0.4875146
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
    double r3i = 113.388;
    double r4i = 266.8343;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4736198, r2 + 0.7246873, r3i + 0.3996184, r4i + 0.8236339
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3405742, r2 + 0.4706920, r3i + 0.2630076, r4i + 0.5038280
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2017308, r2 + 0.2929059, r3i + 0.1273689, r4i + 0.2418999
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2022860, r2 - 0.3073639, r3i - 0.1493857, r4i - 0.3137578
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3158145, r2 - 0.4853530, r3i - 0.2661753, r4i - 0.5406646
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.4589221, r2 - 0.6933638, r3i - 0.3432080, r4i - 0.6990562
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
    double r3i = 114.4817;
    double r4i = 268.0543;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.5659504, r2 + 0.8062122, r3i + 0.4331362, r4i + 0.9011804
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3607682, r2 + 0.5221096, r3i + 0.2652472, r4i + 0.5129480
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1957776, r2 + 0.2977777, r3i + 0.1434561, r4i + 0.2534773
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2358813, r2 - 0.3368890, r3i - 0.1788624, r4i - 0.3370419
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3983539, r2 - 0.5668518, r3i - 0.2933132, r4i - 0.5682160
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.5301981, r2 - 0.7722706, r3i - 0.4191240, r4i - 0.8234377
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
    double r3i = 115.7865;
    double r4i = 269.4054;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.7350223, r2 + 0.8762090, r3i + 0.4777782, r4i + 0.8684542
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3846955, r2 + 0.4891318, r3i + 0.2851597, r4i + 0.4901469
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2031664, r2 + 0.2626881, r3i + 0.1277957, r4i + 0.2325060
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1774332, r2 - 0.2974556, r3i - 0.1827334, r4i - 0.2830384
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.4326267, r2 - 0.5816926, r3i - 0.3033621, r4i - 0.5644906
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.5808185, r2 - 0.7540085, r3i - 0.4165921, r4i - 0.7592115
                    ))
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
    double r3i = 117.0994;
    double r4i = 271.2746;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.7048978, r2 + 0.9405241, r3i + 0.4781417, r4i + 0.8960158
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3255025, r2 + 0.5036681, r3i + 0.2336026, r4i + 0.4729148
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2196114, r2 + 0.2946281, r3i + 0.1472687, r4i + 0.2461419
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2184635, r2 - 0.3073761, r3i - 0.1621991, r4i - 0.2963186
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.4450089, r2 - 0.5720805, r3i - 0.3407231, r4i - 0.5648215
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.5857961, r2 - 0.7534573, r3i - 0.4614198, r4i - 0.7934153
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
    double r3i = 117.3771;
    double r4i = 275.011;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.5808250, r2 + 1.0033374, r3i + 0.4661905, r4i + 1.0007900
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3482697, r2 + 0.4832408, r3i + 0.2316988, r4i + 0.5841193
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1786155, r2 + 0.2974441, r3i + 0.1419674, r4i + 0.2591634
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2097833, r2 - 0.3400561, r3i - 0.1801498, r4i - 0.3221096
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3222572, r2 - 0.7079429, r3i - 0.2959714, r4i - 0.6066239
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.5164597, r2 - 0.8535159, r3i - 0.4125246, r4i - 0.7873700
                    ))
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
    double r3i = 115.4707;
    double r4i = 272.2968;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.5911788, r2 + 0.7754185, r3i + 0.4784439, r4i + 0.9776335
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4072341, r2 + 0.5444370, r3i + 0.2314512, r4i + 0.4582437
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1888553, r2 + 0.2670350, r3i + 0.1088660, r4i + 0.2261400
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2278112, r2 - 0.3546353, r3i - 0.1756106, r4i - 0.3522333
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3912851, r2 - 0.5829698, r3i - 0.2846284, r4i - 0.6681590
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.5983863, r2 - 0.8593577, r3i - 0.3949720, r4i - 0.8329951
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
    double r3i = 114.0486;
    double r4i = 269.0352;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.5988948, r2 + 0.8778407, r3i + 0.4218596, r4i + 0.8905689
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3537385, r2 + 0.5369471, r3i + 0.2330180, r4i + 0.5085732
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1808150, r2 + 0.2610289, r3i + 0.1272996, r4i + 0.2609990
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2075135, r2 - 0.3358505, r3i - 0.1324897, r4i - 0.3279207
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3965947, r2 - 0.5600700, r3i - 0.2696579, r4i - 0.5725696
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.5151627, r2 - 0.7666777, r3i - 0.3847705, r4i - 0.7671337
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
    double r3i = 113.1362;
    double r4i = 267.1021;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.4932189, r2 + 0.7325382, r3i + 0.3626951, r4i + 0.7341333
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2623814, r2 + 0.3852768, r3i + 0.2291263, r4i + 0.4341426
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1417828, r2 + 0.2033706, r3i + 0.1170943, r4i + 0.2305203
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1503256, r2 - 0.2555846, r3i - 0.1471368, r4i - 0.2778598
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3478208, r2 - 0.5186373, r3i - 0.2452785, r4i - 0.4764961
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.4372682, r2 - 0.6724585, r3i - 0.2720416, r4i - 0.5963266
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
    double r3i = 112.3889;
    double r4i = 264.1205;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.3819845, r2 + 0.5967298, r3i + 0.2994325, r4i + 0.6629934
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.2653264, r2 + 0.3758375, r3i + 0.1906555, r4i + 0.3839460
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.1395189, r2 + 0.1703012, r3i + 0.0972906, r4i + 0.2234449
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.1709272, r2 - 0.2402450, r3i - 0.1179910, r4i - 0.2306354
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.2710274, r2 - 0.3760844, r3i - 0.2090779, r4i - 0.4000941
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.3458873, r2 - 0.5330884, r3i - 0.2606778, r4i - 0.4919937
                    ))
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
    double r3i = 111.8035;
    double r4i = 262.8463;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.35754724, r2 + 0.4671662, r3i + 0.24448789, r4i + 0.4861172
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.22453965, r2 + 0.2769617, r3i + 0.16038527, r4i + 0.3172217
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.07846058, r2 + 0.1505218, r3i + 0.07997629, r4i + 0.1797743
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.11116876, r2 - 0.1679319, r3i - 0.06775974, r4i - 0.1999775
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.24125736, r2 - 0.2727228, r3i - 0.14560574, r4i - 0.2420423
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.33289038, r2 - 0.3423280, r3i - 0.21415001, r4i - 0.3308223
                    ))
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
    double r3i = 111.2942;
    double r4i = 262.0845;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 3).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.25064381, r2 + 0.14096480, r3i + 0.15239206, r4i + 0.11945160
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.20491504, r2 + 0.13028252, r3i + 0.06760963, r4i + 0.06996971
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 + 0.05888929, r2 + 0.06870984, r3i + 0.02469856, r4i + 0.07540596
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.13626734, r2 - 0.10934137, r3i - 0.04746213, r4i - 0.05802416
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 2).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.17772637, r2 - 0.09705815, r3i - 0.06056651, r4i - 0.03010615
                    )),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21 * 4).system4(8.0)
                    .ofOhms(fixOhms(
                        r1, r2, r3i, r4i,
                        r1 - 0.33563704, r2 - 0.07558674, r3i - 0.23192280, r4i - 0.12781794
                    ))
            )
        )
    );
  }
}
