package com.ak.comm.converter.rsce;

import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.log.LogTestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import javax.annotation.ParametersAreNonnullByDefault;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RsceConverterTest {
  private static final Logger LOGGER = Logger.getLogger(RsceConverter.class.getName());

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#infoRequests")
  @ParametersAreNonnullByDefault
  void testApply(byte[] bytes, int[] rDozenMilliOhms, int[] infoOnes) {
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build();
    assertNotNull(frame);
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
                assertThat(logRecord.getMessage()).contains(rsceVariable.name());
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