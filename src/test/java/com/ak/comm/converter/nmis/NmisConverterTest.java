package com.ak.comm.converter.nmis;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.nmis.NmisAddress;
import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variables;
import com.ak.comm.log.LogTestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_VALUES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class NmisConverterTest {
  private static final Logger LOGGER = Logger.getLogger(NmisConverter.class.getName());
  private static final int[] EMPTY_INTS = {};

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.nmis.NmisTestProvider#aliveAndChannelsResponse")
  @ParametersAreNonnullByDefault
  void testAliveAndChannelsResponse(NmisAddress address, byte[] input) {
    testConverter(address, input, EMPTY_INTS);
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.nmis.NmisTestProvider#dataResponse")
  @ParametersAreNonnullByDefault
  void testDataResponse(byte[] input, int[] expected) {
    testConverter(NmisAddress.DATA, input, expected);
  }

  @ParametersAreNonnullByDefault
  private static void testConverter(NmisAddress address, byte[] input, int[] expected) {
    NmisResponseFrame frame = new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build();
    if (NmisAddress.ALIVE == address) {
      assertNull(frame);
    }
    else {
      assertNotNull(frame);
      assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LOG_LEVEL_VALUES, () -> {
        Converter<NmisResponseFrame, NmisVariable> converter = new NmisConverter();
        Stream<int[]> stream = converter.apply(frame);
        if (expected.length == 0) {
          assertThat(stream.count()).isZero();
        }
        else {
          stream.forEach(ints -> assertThat(ints).containsExactly(expected));
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