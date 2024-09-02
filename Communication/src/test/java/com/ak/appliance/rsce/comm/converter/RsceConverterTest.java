package com.ak.appliance.rsce.comm.converter;

import com.ak.appliance.rsce.comm.bytes.RsceCommandFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variables;
import com.ak.comm.logging.LogTestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_VALUES;
import static org.assertj.core.api.Assertions.assertThat;

class RsceConverterTest {
  private static final Logger LOGGER = Logger.getLogger(RsceConverter.class.getName());

  @ParameterizedTest
  @MethodSource("com.ak.appliance.rsce.comm.bytes.RsceTestDataProvider#infoRequests")
  void testApply(byte[] bytes, int[] rDozenMilliOhms, int[] infoOnes) {
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build().orElseThrow();
    assertThat(
        LogTestUtils.isSubstituteLogLevel(LOGGER, LOG_LEVEL_VALUES,
            () -> {
              Converter<RsceCommandFrame, RsceVariable> converter = new RsceConverter();
              Stream<int[]> stream = converter.apply(frame);
              assertThat(stream).isNotEmpty().allSatisfy(
                  ints -> assertThat(ints).containsExactly(
                      Stream.of(
                          Arrays.stream(rDozenMilliOhms),
                          Arrays.stream(infoOnes),
                          IntStream.of(0, 0, 0)
                      ).flatMapToInt(Function.identity()).toArray()
                  )
              );
            },
            logRecord -> {
              for (int milliOhm : rDozenMilliOhms) {
                assertThat(logRecord.getMessage()).contains(String.format(Locale.getDefault(), "%,d", milliOhm));
              }
              for (RsceVariable rsceVariable : RsceVariable.values()) {
                assertThat(logRecord.getMessage()).contains(Variables.toString(rsceVariable));
                if (EnumSet.of(RsceVariable.ACCELEROMETER, RsceVariable.FINGER_CLOSED).contains(rsceVariable)) {
                  assertThat(rsceVariable.getUnit()).isEqualTo(AbstractUnit.ONE);
                }
                else if (EnumSet.of(RsceVariable.R1, RsceVariable.R2).contains(rsceVariable)) {
                  assertThat(rsceVariable.getUnit()).isEqualTo(MetricPrefix.CENTI(Units.OHM));
                }
                else {
                  assertThat(rsceVariable.getUnit()).isEqualTo(Units.PERCENT);
                }
              }
            }
        ))
        .isEqualTo(rDozenMilliOhms.length > 0 && infoOnes.length > 0);
  }
}