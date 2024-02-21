package com.ak.comm.interceptor.purelogic;

import com.ak.comm.bytes.LogUtils;
import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.log.LogTestUtils;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PureLogicBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(PureLogicBytesInterceptor.class.getName());
  private static final int[] EMPTY = {};

  @Test
  void test() {
    testResponse(
        "  STEP+ 00320  \r\n".getBytes(StandardCharsets.UTF_8),
        new int[] {300}, true);
    testResponse(
        new byte[] {83, 84, 69, 80, 43, 32, 48, 48, 51, 50, 48, 32, 32, 13, 10},
        new int[] {300}, true);
    testResponse(
        "STEP+ 00016  \r\nSTEP- 00016  \r\n".getBytes(StandardCharsets.UTF_8),
        new int[] {15, -15}, true);
    testResponse(
        "STEP+ 00dxx  \r\n".getBytes(StandardCharsets.UTF_8),
        EMPTY, false);
  }

  private static void testResponse(byte[] input, int[] expected, boolean logFlag) {
    Function<ByteBuffer, Stream<PureLogicFrame>> interceptor = new PureLogicBytesInterceptor();
    assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES, () -> {
      List<PureLogicFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).toList();
      if (!frames.isEmpty()) {
        assertThat(frames.stream().mapToInt(PureLogicFrame::getMicrons).toArray())
            .withFailMessage(() -> frames.stream().map(PureLogicFrame::toString).collect(Collectors.joining()))
            .containsExactly(expected);
      }
    }, logRecord -> assertThat(logRecord.getMessage()).contains(PureLogicFrame.class.getSimpleName())), logFlag);
  }
}