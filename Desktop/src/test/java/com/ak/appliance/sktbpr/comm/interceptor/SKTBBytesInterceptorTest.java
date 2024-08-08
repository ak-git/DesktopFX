package com.ak.appliance.sktbpr.comm.interceptor;

import com.ak.appliance.sktbpr.comm.bytes.SKTBResponse;
import com.ak.comm.bytes.LogUtils;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.log.LogTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SKTBBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(SKTBBytesInterceptor.class.getName());

  static Stream<Arguments> response() {
    return Stream.of(
        arguments(new byte[] {-91, 1, 6, 0, 0, 0, 0, -84, 10}, true),
        arguments(new byte[] {-92, 1, 7, 0, 0, 0, 0, -84, 10}, false)
    );
  }

  @ParameterizedTest
  @MethodSource("response")
  void testResponse(byte[] input, boolean ok) {
    Function<ByteBuffer, Stream<SKTBResponse>> interceptor = new SKTBBytesInterceptor();

    assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES, () -> {
      List<SKTBResponse> frames = interceptor.apply(ByteBuffer.wrap(input)).toList();

      assertThat(frames.isEmpty()).isNotEqualTo(ok);
      if (!frames.isEmpty()) {
        assertThat(frames).hasSize(1);
      }
    }, logRecord -> assertTrue(logRecord.getMessage().endsWith("9 bytes"))), ok);
  }

  @Test
  void testSerialParams() {
    assertThat(new SKTBBytesInterceptor().getSerialParams()).containsExactly(BytesInterceptor.SerialParams.ODD_PARITY);
  }
}