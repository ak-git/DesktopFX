package com.ak.comm.interceptor.suntech;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.LogUtils;
import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.log.LogTestUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NIBPBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(NIBPBytesInterceptor.class.getName());
  private static final int[] EMPTY = {};

  @Test
  void test() {
    testResponse(new byte[] {0x3E, 0x05, 0x02, 0x01, (byte) 0xBA}, new int[] {258}, true);
    testResponse(new byte[] {0x3E, 0x05, 0x02, 0x01, (byte) 0xB1}, EMPTY, false);
    testResponse(new byte[] {0x3E, 0x04, 0x4B, 0x73}, EMPTY, true);
    testResponse(
        // The Module will reply with a data packet containing 21 data bytes,
        // consisting of systolic, diastolic, heart rate, and other parameter data.
        // Total packet - 24 bytes
        new byte[] {
            0x3e, // MODULE START BYTE = the ">" character (0x3E)
            0x18, // 24 - total number of bytes in the packet

            0x73, 0x00, // 115 - Systolic value in mmHg (unsigned integer, LSB first)
            0x4a, 0x00, // 74 - Diastolic value in mmHg (unsigned integer, LSB first)
            0x1e, // 30 - Number of Heart Beats detected during the BP sample (unsigned byte)
            0x01, // BP Status (unsigned byte)

            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x31, 0x07, // Test Codes [ ] (8 unsigned bytes, TC [0] through TC [7] )
            0x55, // 85 - Heart Rate in beats per minute (unsigned byte)
            0x00, // Spare byte (not used)
            0x58, 0x00, // 88 - Mean Arterial Pressure (MAP) in mmHg (unsigned integer, LSB first)
            0x00, // Error Code (unsigned byte)
            0x00, // Spare byte (not used)
            0x00, // Spare byte (not used)
            (byte) 0xe9 // 0x100 - modulo 256 (Start byte + Packet byte + Data bytes)
        }, new int[] {115, 74, 85, 88}, true);
  }

  @ParametersAreNonnullByDefault
  private static void testResponse(byte[] input, int[] expected, boolean logFlag) {
    Function<ByteBuffer, Stream<NIBPResponse>> interceptor = new NIBPBytesInterceptor();
    assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES, () -> {
      List<NIBPResponse> frames = interceptor.apply(ByteBuffer.wrap(input)).toList();
      if (!frames.isEmpty()) {
        frames.get(0).extractPressure(value -> assertThat(value).isEqualTo(expected[0]));
        frames.get(0).extractData(value -> assertThat(value).containsExactly(expected));
        frames.get(0).extractIsCompleted(() -> {
        });
      }
    }, logRecord -> assertThat(logRecord.getMessage()).contains(NIBPResponse.class.getSimpleName())), logFlag);
  }
}