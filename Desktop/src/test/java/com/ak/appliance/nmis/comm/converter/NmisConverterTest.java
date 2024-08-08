package com.ak.appliance.nmis.comm.converter;

import com.ak.appliance.nmis.comm.bytes.NmisAddress;
import com.ak.appliance.nmis.comm.bytes.NmisResponseFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variables;
import com.ak.comm.log.LogTestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_VALUES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NmisConverterTest {
  private static final Logger LOGGER = Logger.getLogger(NmisConverter.class.getName());
  private static final int[] EMPTY_INTS = {};

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#aliveAndChannelsResponse")
  void testAliveAndChannelsResponse(NmisAddress address, byte[] input) {
    testConverter(address, input, EMPTY_INTS);
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#dataResponse")
  void testDataResponse(byte[] input, int[] expected) {
    testConverter(NmisAddress.DATA, input, expected);
  }

  private static void testConverter(NmisAddress address, byte[] input, int[] expected) {
    Optional<NmisResponseFrame> frame = new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build();
    if (NmisAddress.ALIVE == address) {
      assertThat(frame).isEmpty();
    }
    else {
      assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LOG_LEVEL_VALUES, () -> {
        Converter<NmisResponseFrame, NmisVariable> converter = new NmisConverter();
        Stream<int[]> stream = converter.apply(frame.orElseThrow());
        if (expected.length == 0) {
          assertThat(stream.count()).isZero();
        }
        else {
          assertThat(stream).isNotEmpty().allSatisfy(ints -> assertThat(ints).containsExactly(expected));
        }
      }, logRecord -> {
        for (int v : expected) {
          assertThat(logRecord.getMessage()).contains(Integer.toString(v));
        }
        for (NmisVariable v : NmisVariable.values()) {
          assertThat(logRecord.getMessage()).contains(Variables.toString(v));
          assertThat(v.getUnit()).isEqualTo(MetricPrefix.MILLI(Units.SECOND));
        }
      }), expected.length > 0);
    }
  }
}