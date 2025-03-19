package com.ak.appliance.purelogic.comm.interceptor;

import com.ak.appliance.purelogic.comm.bytes.PureLogicFrame;
import com.ak.comm.bytes.LogUtils;
import com.ak.comm.logging.LogTestUtils;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PureLogicBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(PureLogicBytesInterceptor.class.getName());
  private static final double[] EMPTY = {};

  @Test
  void test() {
    testResponse(
        "  STEP+ 00320  \r\n".getBytes(StandardCharsets.UTF_8),
        new double[] {300.0}, true);
    testResponse(
        new byte[] {83, 84, 69, 80, 43, 32, 48, 48, 51, 50, 48, 32, 32, 13, 10},
        new double[] {300.0}, true);
    testResponse(
        "STEP+ 00016  \r\nSTEP- 00016  \r\n".getBytes(StandardCharsets.UTF_8),
        new double[] {15.0, -15.0}, true);
    testResponse(
        "STEP+ 00008  \r\nSTEP- 00008  \r\n".getBytes(StandardCharsets.UTF_8),
        new double[] {7.5, -7.5}, true);
    testResponse(
        "STEP+ 00dxx  \r\n".getBytes(StandardCharsets.UTF_8),
        EMPTY, false);
  }

  private static void testResponse(byte[] input, double[] expected, boolean logFlag) {
    Function<ByteBuffer, Stream<PureLogicFrame>> interceptor = new PureLogicBytesInterceptor(PureLogicBytesInterceptor.class.getSimpleName());
    assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES, () -> {
      List<PureLogicFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).toList();
      if (!frames.isEmpty()) {
        assertThat(frames.stream().mapToDouble(PureLogicFrame::getMicrons).toArray())
            .withFailMessage(() -> frames.stream().map(PureLogicFrame::toString).collect(Collectors.joining()))
            .containsExactly(expected, withPrecision(0.1));
      }
    }, logRecord -> assertThat(logRecord.getMessage()).contains(PureLogicFrame.class.getSimpleName())), logFlag);
  }
}