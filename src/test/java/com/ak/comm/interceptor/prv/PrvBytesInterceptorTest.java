package com.ak.comm.interceptor.prv;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.core.LogUtils;
import com.ak.comm.log.LogTestUtils;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PrvBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(PrvBytesInterceptor.class.getName());
  private static final int[] EMPTY = {};

  @Test
  public void test() {
    testResponse(
        new byte[] {
            '\r',
            '\n', 51, 102, 102, 53, '\r',
            '\n', 51,
        },
        new int[] {16373}, true);
    testResponse(
        "invalid invalid invalid invalid".getBytes(StandardCharsets.UTF_8),
        EMPTY, false);
  }

  @ParametersAreNonnullByDefault
  private static void testResponse(byte[] input, int[] expected, boolean logFlag) {
    Function<ByteBuffer, Stream<BufferFrame>> interceptor = new PrvBytesInterceptor();
    Assert.assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES, () -> {
      List<BufferFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).collect(Collectors.toList());
      if (!frames.isEmpty()) {
        Assert.assertEquals(
            frames.stream().mapToInt(value -> {
              StringBuilder sb = new StringBuilder(Integer.BYTES);
              for (int i = 0; i < Integer.BYTES; i++) {
                sb.append((char) value.get(i + 1));
              }
              return Integer.parseInt(sb.toString(), 16);
            }).toArray(), expected,
            frames.stream().map(BufferFrame::toString).collect(Collectors.joining()));
      }
    }, logRecord -> Assert.assertNotNull(
        logRecord.getMessage().replaceAll(".*" + BufferFrame.class.getSimpleName(), Strings.EMPTY)
    )), logFlag);
  }
}