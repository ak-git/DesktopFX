package com.ak.comm.interceptor.purelogic;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.core.LogUtils;
import com.ak.comm.log.LogTestUtils;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PureLogicBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(PureLogicBytesInterceptor.class.getName());
  private static final int[] EMPTY = {};

  @Test
  public void test() {
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

  @ParametersAreNonnullByDefault
  private static void testResponse(byte[] input, int[] expected, boolean logFlag) {
    Function<ByteBuffer, Stream<PureLogicFrame>> interceptor = new PureLogicBytesInterceptor();
    Assert.assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES, () -> {
      List<PureLogicFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).collect(Collectors.toList());
      if (!frames.isEmpty()) {
        Assert.assertEquals(frames.stream().mapToInt(PureLogicFrame::getMicrons).toArray(), expected,
            frames.stream().map(PureLogicFrame::toString).collect(Collectors.joining()));
      }
    }, logRecord -> Assert.assertNotNull(
        logRecord.getMessage().replaceAll(".*" + PureLogicFrame.class.getSimpleName(), Strings.EMPTY)
    )), logFlag);
  }
}