package com.ak.comm.interceptor.simple;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.bytes.LogUtils;
import com.ak.comm.logging.LogTestUtils;
import com.ak.util.Strings;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

final class FrameBytesInterceptorDataProvider {
  private final ByteBuffer buffer = ByteBuffer.allocate(20);

  static Stream<Arguments> rampData() {
    return Stream.of(
        arguments(
            // invalid first byte
            new byte[] {0x7f,
                (byte) 255, 4, 3, 2, 1, 4, 3, 2, 1,
                0, 9, 10, 11, 12, 13, 14, 15, 16
            },
            new BufferFrame(new byte[] {
                (byte) 255, 4, 3, 2, 1, 4, 3, 2, 1
            }, ByteOrder.nativeOrder()),
            "[ 0x7f ] IGNORED"
        ),
        arguments(
            // check 127, -128 ramp step
            new byte[] {
                0x7f, 17, 18, 19, 20, 21, 22, 23, 24,
                (byte) 0x80, 25, 26, 27, 28, 29, 30, 31, 32
            },
            new BufferFrame(new byte[] {
                (byte) 127, 17, 18, 19, 20, 21, 22, 23, 24
            }, ByteOrder.nativeOrder()),
            "[ 0x00, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10 ] 9 bytes IGNORED"
        ),
        arguments(
            new byte[] {
                (byte) 0xc2, 0x04, 0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01,
                (byte) 0xc3, 0x04
            },
            new BufferFrame(new byte[] {
                (byte) 0xc2, 0x04, 0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01
            }, ByteOrder.nativeOrder()),
            "[ 0x80, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x20 ] 9 bytes IGNORED"
        ),
        arguments(
            new byte[] {
                0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01, 0x01, 0x09,
                0x04, 0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01,
            },
            new BufferFrame(new byte[] {
                0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01, 0x01, 0x09
            }, ByteOrder.nativeOrder()),
            "[ 0xc3, 0x04 ] 2 bytes IGNORED"
        ),
        arguments(
            new byte[] {
                0x0a, 0x04, 0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01,
                0x0b
            },
            new BufferFrame(new byte[] {
                0x0a, 0x04, 0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01,
            }, ByteOrder.nativeOrder()),
            "[ 0x04, 0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01 ] 8 bytes IGNORED"
        )
    );
  }

  static Stream<Arguments> fixedStartData() {
    return Stream.of(
        arguments(
            // invalid first byte
            new byte[] {0x7f,
                (byte) 255, 4, 3, 2, 1, 4, 3, 2, 1,
                (byte) 255, 9, 10, 11, 12, 13, 14, 15, 16
            },
            new BufferFrame(new byte[] {
                (byte) 255, 4, 3, 2, 1, 4, 3, 2, 1
            }, ByteOrder.nativeOrder()),
            "[ 0x7f ] IGNORED"
        ),
        arguments(
            new byte[] {
                0x7f, 17, 18, 19, 20, 21, 22, 23, 24,
                0x7f, 25, 26, 27, 28, 29, 30, 31, 32
            },
            new BufferFrame(new byte[] {
                0x7f, 17, 18, 19, 20, 21, 22, 23, 24
            }, ByteOrder.nativeOrder()),
            "[ 0xff, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10 ] 9 bytes IGNORED"
        ),
        arguments(
            new byte[] {
                (byte) 0xc2, 0x04, 0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01,
                (byte) 0xc2, 0x04
            },
            new BufferFrame(new byte[] {
                (byte) 0xc2, 0x04, 0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01
            }, ByteOrder.nativeOrder()),
            "[ 0x7f, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x20 ] 9 bytes IGNORED"
        ),
        arguments(
            new byte[] {
                0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01, 0x01, 0x09,
                0x03, 0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01,
            },
            new BufferFrame(new byte[] {
                0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01, 0x01, 0x09
            }, ByteOrder.nativeOrder()),
            "[ 0xc2, 0x04 ] 2 bytes IGNORED"
        ),
        arguments(
            new byte[] {
                0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01, 0x0a, 0x04,
                0x03
            },
            new BufferFrame(new byte[] {
                0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01, 0x0a, 0x04,
            }, ByteOrder.nativeOrder()),
            "[ 0x03, 0x03, 0x02, 0x01, 0x04, 0x03, 0x02, 0x01 ] 8 bytes IGNORED"
        )
    );
  }

  <T> void testInterceptor(byte[] bytes, T response, CharSequence ignoredMessage,
                           Logger logger, Function<ByteBuffer, Stream<T>> interceptor) {
    buffer.clear();
    buffer.put(bytes);
    buffer.flip();

    AtomicReference<String> logMessage = new AtomicReference<>(Strings.EMPTY);
    assertTrue(LogTestUtils.isSubstituteLogLevel(logger, LogUtils.LOG_LEVEL_ERRORS,
        () -> {
          Stream<T> frames = interceptor.apply(buffer);
          assertThat(frames).containsExactly(response);
          assertThat(logMessage.get()).endsWith(ignoredMessage);
        },
        logRecord -> logMessage.set(logRecord.getMessage())
    ));
  }
}
