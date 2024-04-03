package com.ak.comm.interceptor.simple;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.BytesInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class StringBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(StringBytesInterceptor.class.getName());
  private static final Function<ByteBuffer, Stream<String>> INTERCEPTOR = new StringBytesInterceptor(StringBytesInterceptorTest.class.getName());

  @Test
  void testInterceptorProperties() {
    BytesInterceptor<BufferFrame, String> interceptor = new StringBytesInterceptor(getClass().getName());
    assertThat(interceptor.getBaudRate()).isEqualTo(115200);
    assertThat(interceptor.getPingRequest()).isEmpty();
  }

  static Stream<Arguments> data() {
    return Stream.of(
        arguments(
            new byte[] {
                '1', '2', '3', '4', '5', '6', 0x37, 0x38, 0x39, 0x30,
                51, 102, 102, 53, '\r', '\n'
            },
            "3ff5",
            "[ 0x37, 0x38, 0x39, 0x30 ] 4 bytes IGNORED"
        )
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  void testInterceptor(byte[] bytes, String response, CharSequence ignoredMessage) {
    new FrameBytesInterceptorDataProvider().testInterceptor(bytes, response, ignoredMessage, LOGGER, INTERCEPTOR);
  }
}