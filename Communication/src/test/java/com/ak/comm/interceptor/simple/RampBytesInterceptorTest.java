package com.ak.comm.interceptor.simple;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.bytes.LogUtils;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.logging.LogTestUtils;
import com.ak.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RampBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(RampBytesInterceptor.class.getName());
  private static final Function<ByteBuffer, Stream<BufferFrame>> INTERCEPTOR =
      new RampBytesInterceptor(RampBytesInterceptorTest.class.getName(), BytesInterceptor.BaudRate.BR_115200, 9);

  @Test
  void testInvalidInterceptorProperties() {
    String name = getClass().getName();
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new RampBytesInterceptor(name, BytesInterceptor.BaudRate.BR_115200, 0));
  }

  @Test
  void testInterceptorProperties() {
    BytesInterceptor<BufferFrame, BufferFrame> interceptor = new RampBytesInterceptor(getClass().getName(), BytesInterceptor.BaudRate.BR_115200, 1);
    assertThat(interceptor.getBaudRate()).isEqualTo(115200);
    assertNull(interceptor.getPingRequest());
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.interceptor.simple.FrameBytesInterceptorDataProvider#rampData")
  @ParametersAreNonnullByDefault
  void testRampBytesInterceptor(byte[] input, BufferFrame testFrame, CharSequence ignoredMessage) {
    BytesInterceptor<BufferFrame, BufferFrame> interceptor = new RampBytesInterceptor(getClass().getName(), BytesInterceptor.BaudRate.BR_921600, 9);

    assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES,
        () -> {
          Collection<BufferFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).toList();
          assertThat(frames).containsExactly(testFrame);
        },
        logRecord -> assertThat(logRecord.getMessage())
            .withFailMessage("[ %s ] must ends with [ %s ]", logRecord.getMessage(), testFrame)
            .endsWith(testFrame.toString())));

    AtomicReference<String> logMessage = new AtomicReference<>(Strings.EMPTY);
    assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
        () -> {
          int bytesOut = interceptor.putOut(testFrame).remaining();
          assertThat(bytesOut).isPositive();
          assertThat(logMessage.get()).isEqualTo(
              "%s - %d bytes OUT to hardware",
              testFrame.toString().replaceAll(".*" + BufferFrame.class.getSimpleName(), Strings.EMPTY), bytesOut
          );
        },
        logRecord -> logMessage.set(logRecord.getMessage().replaceAll(".*" + BufferFrame.class.getSimpleName(), Strings.EMPTY))));

    BufferFrame singleByte = new BufferFrame(new byte[] {input[0]}, ByteOrder.nativeOrder());
    assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
        () -> assertThat(interceptor.putOut(singleByte).remaining()).isPositive(),
        logRecord -> assertThat(logRecord.getMessage()).endsWith(singleByte + " - OUT to hardware")));

    assertThat(ignoredMessage).isNotEmpty();
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.interceptor.simple.FrameBytesInterceptorDataProvider#rampData")
  @ParametersAreNonnullByDefault
  void testInterceptor(byte[] bytes, BufferFrame response, CharSequence ignoredMessage) {
    new FrameBytesInterceptorDataProvider().testInterceptor(bytes, response, ignoredMessage, LOGGER, INTERCEPTOR);
  }
}