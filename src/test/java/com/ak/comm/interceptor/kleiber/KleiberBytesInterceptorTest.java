package com.ak.comm.interceptor.kleiber;

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

public class KleiberBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(KleiberBytesInterceptor.class.getName());
  private static final double[] EMPTY = {};

  @Test
  public void test() {
    testResponse(
        new byte[] {
            (byte) 0xaa,
            0, 0, (byte) 0x80, 0x3f,
            0, 0, (byte) 0x80, 0x3f,
            0, 0, (byte) 0x80, 0x3f,
            0, 0, (byte) 0x80, 0x3f,
            0, 0, (byte) 0x80, 0x3f,
            0, 0, (byte) 0x80, 0x3f,
            0, 0, (byte) 0x80, 0x3f,
            0, 0, (byte) 0x80, 0x3f,
            (byte) 0xbb,

            (byte) 0xaa,
        },
        new double[] {1}, true);
    testResponse(
        "invalid invalid invalid invalid".getBytes(StandardCharsets.UTF_8),
        EMPTY, false);
  }

  @ParametersAreNonnullByDefault
  private static void testResponse(byte[] input, double[] expected, boolean logFlag) {
    Function<ByteBuffer, Stream<BufferFrame>> interceptor = new KleiberBytesInterceptor();
    Assert.assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES, () -> {
      List<BufferFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).collect(Collectors.toList());
      if (!frames.isEmpty()) {
        Assert.assertEquals(frames.stream().mapToDouble(value -> value.getFloat(1)).toArray(), expected, 0.001,
            frames.stream().map(BufferFrame::toString).collect(Collectors.joining()));
      }
    }, logRecord -> Assert.assertNotNull(
        logRecord.getMessage().replaceAll(".*" + BufferFrame.class.getSimpleName(), Strings.EMPTY)
    )), logFlag);
  }
}