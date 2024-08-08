package com.ak.appliance.rcm.comm.interceptor;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.bytes.LogUtils;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.log.LogTestUtils;
import com.ak.util.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RcmBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(RcmBytesInterceptor.class.getName());
  private final BytesInterceptor<BufferFrame, BufferFrame> interceptor = new RcmBytesInterceptor();
  private final ByteBuffer buffer = ByteBuffer.allocate(100);


  static Stream<Arguments> data() {
    return Stream.of(
        arguments(
            new byte[] {
                (byte) 0xf7, (byte) 0xf9, (byte) 0x81, (byte) 0x81, (byte) 0xfb, (byte) 0xc1, (byte) 0x81, (byte) 0xe9, (byte) 0x81, (byte) 0x81, //  invalid data
                -10, -36, -125, -72, -5, -60, -125, -124, -111, -94, -7, -98, -127, -128, -5, -78, -127, -10, -127, -128,
                -10,
            },
            new BufferFrame(new byte[] {
                (byte) 0xf6, (byte) 0xdc, (byte) 0x83, (byte) 0xb8, (byte) 0xfb, (byte) 0xc4, (byte) 0x83, (byte) 0x84,
                (byte) 0x91, (byte) 0xa2, (byte) 0xf9, (byte) 0x9e, (byte) 0x81, (byte) 0x80, (byte) 0xfb, (byte) 0xb2, (byte) 0x81, (byte) 0xf6, (byte) 0x81, (byte) 0x80
            }, ByteOrder.LITTLE_ENDIAN),
            "[ 0xf7, 0xf9, 0x81, 0x81, 0xfb, 0xc1, 0x81, 0xe9, 0x81, 0x81 ] 10 bytes IGNORED"
        )
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  void testInterceptor(byte[] bytes, BufferFrame response, CharSequence ignoredMessage) {
    buffer.clear();
    buffer.put(bytes);
    buffer.flip();

    Assertions.assertAll(interceptor.toString(),
        () -> assertThat(interceptor.getBaudRate()).isEqualTo(115200 / 3),
        () -> assertThat(interceptor.getPingRequest()).isEmpty(),
        () -> assertThat(interceptor.getSerialParams())
            .containsExactly(BytesInterceptor.SerialParams.CLEAR_DTR, BytesInterceptor.SerialParams.DATA_BITS_7)
    );


    AtomicReference<String> logMessage = new AtomicReference<>(Strings.EMPTY);
    assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
        () -> {
          Stream<BufferFrame> frames = interceptor.apply(buffer);
          assertThat(frames).containsExactly(response);
          assertThat(logMessage.get()).endsWith(ignoredMessage);
        },
        logRecord -> logMessage.set(logRecord.getMessage())
    ));
  }
}
