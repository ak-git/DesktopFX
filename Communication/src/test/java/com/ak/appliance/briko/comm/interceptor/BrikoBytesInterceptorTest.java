package com.ak.appliance.briko.comm.interceptor;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.bytes.LogUtils;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.logging.LogTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class BrikoBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(BrikoBytesInterceptor.class.getName());

  @Test
  void testInterceptorProperties() {
    BytesInterceptor<BufferFrame, BufferFrame> interceptor = new BrikoBytesInterceptor();
    assertThat(interceptor.name()).isEqualTo("Briko-Stand");
    assertThat(interceptor.getBaudRate()).isEqualTo(115200 * 8);
    assertThat(interceptor.getPingRequest()).isEmpty();
  }


  static Stream<Arguments> data() {
    return Stream.of(
        arguments(
            // invalid first byte
            new byte[] {0x7f,
                0x74, 0x20,
                (byte) 0xC1, 0x02, (byte) 0x8A, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xC2, (byte) 0xCB, 0x15, 0x00, 0x00,
                (byte) 0xC3, 0x1F, 0x0F, 0x00, 0x00,
                (byte) 0xC4, (byte) 0xF1, 0x07, 0x00, 0x00,
                (byte) 0xC5, 0x57, 0x26, 0x00, 0x00,
                (byte) 0xC6, (byte) 0xD8, 0x03, 0x00, 0x00,

                0x75, 0x20,
                (byte) 0xC1, (byte) 0x96, (byte) 0x89, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xC2, (byte) 0xE0, 0x14, 0x00, 0x00,
                (byte) 0xC3, 0x18, 0x0F, 0x00, 0x00,
                (byte) 0xC4, (byte) 0xF5, 0x07, 0x00, 0x00,
                (byte) 0xC5, 0x57, 0x26, 0x00, 0x00,
                (byte) 0xC6, (byte) 0xD9, 0x03, 0x00, 0x00
            },
            new BufferFrame(new byte[] {
                0x74, 0x20,
                (byte) 0xC1, 0x02, (byte) 0x8A, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xC2, (byte) 0xCB, 0x15, 0x00, 0x00,
                (byte) 0xC3, 0x1F, 0x0F, 0x00, 0x00,
                (byte) 0xC4, (byte) 0xF1, 0x07, 0x00, 0x00,
                (byte) 0xC5, 0x57, 0x26, 0x00, 0x00,
                (byte) 0xC6, (byte) 0xD8, 0x03, 0x00, 0x00
            }, ByteOrder.nativeOrder())
        ),
        arguments(
            // invalid ramp step
            new byte[] {
                0x74, 0x20,
                (byte) 0xC1, 0x02, (byte) 0x8A, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xC2, (byte) 0xCB, 0x15, 0x00, 0x00,
                (byte) 0xC3, 0x1F, 0x0F, 0x00, 0x00,
                (byte) 0xC4, (byte) 0xF1, 0x07, 0x00, 0x00,
                (byte) 0xC5, 0x57, 0x26, 0x00, 0x00,
                (byte) 0xC6, (byte) 0xD8, 0x03, 0x00, 0x00,

                0x74, 0x20,
                (byte) 0xC1, (byte) 0x96, (byte) 0x89, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xC2, (byte) 0xE0, 0x14, 0x00, 0x00,
                (byte) 0xC3, 0x18, 0x0F, 0x00, 0x00,
                (byte) 0xC4, (byte) 0xF5, 0x07, 0x00, 0x00,
                (byte) 0xC5, 0x57, 0x26, 0x00, 0x00,
                (byte) 0xC6, (byte) 0xD9, 0x03, 0x00, 0x00,

                0x75, 0x20,
                (byte) 0xC1, (byte) 0x96, (byte) 0x89, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xC2, (byte) 0xE0, 0x14, 0x00, 0x00,
                (byte) 0xC3, 0x18, 0x0F, 0x00, 0x00,
                (byte) 0xC4, (byte) 0xF5, 0x07, 0x00, 0x00,
                (byte) 0xC5, 0x57, 0x26, 0x00, 0x00,
                (byte) 0xC6, (byte) 0xD9, 0x03, 0x00, 0x00
            },
            new BufferFrame(new byte[] {
                0x74, 0x20,
                (byte) 0xC1, (byte) 0x96, (byte) 0x89, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xC2, (byte) 0xE0, 0x14, 0x00, 0x00,
                (byte) 0xC3, 0x18, 0x0F, 0x00, 0x00,
                (byte) 0xC4, (byte) 0xF5, 0x07, 0x00, 0x00,
                (byte) 0xC5, 0x57, 0x26, 0x00, 0x00,
                (byte) 0xC6, (byte) 0xD9, 0x03, 0x00, 0x00
            }, ByteOrder.nativeOrder())
        ),
        arguments(
            // invalid channel step
            new byte[] {
                0x74, 0x20,
                (byte) 0xC1, 0x02, (byte) 0x8A, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xC1, (byte) 0xCB, 0x15, 0x00, 0x00,
                (byte) 0xC3, 0x1F, 0x0F, 0x00, 0x00,
                (byte) 0xC4, (byte) 0xF1, 0x07, 0x00, 0x00,
                (byte) 0xC5, 0x57, 0x26, 0x00, 0x00,
                (byte) 0xC6, (byte) 0xD8, 0x03, 0x00, 0x00,

                0x75, 0x20,
                (byte) 0xC1, (byte) 0x96, (byte) 0x89, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xC2, (byte) 0xE0, 0x14, 0x00, 0x00,
                (byte) 0xC3, 0x18, 0x0F, 0x00, 0x00,
                (byte) 0xC4, (byte) 0xF5, 0x07, 0x00, 0x00,
                (byte) 0xC5, 0x57, 0x26, 0x00, 0x00,
                (byte) 0xC6, (byte) 0xD9, 0x03, 0x00, 0x00,

                0x76, 0x20,
                (byte) 0xC1, (byte) 0x96, (byte) 0x89, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xC2, (byte) 0xE0, 0x14, 0x00, 0x00,
                (byte) 0xC3, 0x18, 0x0F, 0x00, 0x00,
                (byte) 0xC4, (byte) 0xF5, 0x07, 0x00, 0x00,
                (byte) 0xC5, 0x57, 0x26, 0x00, 0x00,
                (byte) 0xC6, (byte) 0xD9, 0x03, 0x00, 0x00
            },
            new BufferFrame(new byte[] {
                0x75, 0x20,
                (byte) 0xC1, (byte) 0x96, (byte) 0x89, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xC2, (byte) 0xE0, 0x14, 0x00, 0x00,
                (byte) 0xC3, 0x18, 0x0F, 0x00, 0x00,
                (byte) 0xC4, (byte) 0xF5, 0x07, 0x00, 0x00,
                (byte) 0xC5, 0x57, 0x26, 0x00, 0x00,
                (byte) 0xC6, (byte) 0xD9, 0x03, 0x00, 0x00
            }, ByteOrder.nativeOrder())
        )
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  void testBrikoBytesInterceptor(byte[] input, BufferFrame testFrame) {
    Function<ByteBuffer, Stream<BufferFrame>> interceptor = new BrikoBytesInterceptor();

    assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES,
        () -> {
          Collection<BufferFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).toList();
          assertThat(frames).containsExactly(testFrame);
        },
        logRecord -> assertThat(logRecord.getMessage())
            .withFailMessage("[ %s ] must ends with [ %s ]", logRecord.getMessage(), testFrame)
            .endsWith(testFrame.toString())));
  }
}