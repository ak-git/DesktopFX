package com.ak.comm.interceptor.suntech;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.log.LogTestUtils;
import com.ak.util.LogUtils;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NIBPBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(NIBPBytesInterceptor.class.getName());
  public static final int[] EMPTY = {};

  @Test
  public void test() {
    testResponse(new byte[] {0x3E, 0x05, 0x02, 0x01, (byte) 0xBA}, new int[] {258}, true);
    testResponse(new byte[] {0x3E, 0x05, 0x02, 0x01, (byte) 0xB1}, EMPTY, false);
  }

  @ParametersAreNonnullByDefault
  private static void testResponse(byte[] input, int[] expected, boolean logFlag) {
    Function<ByteBuffer, Stream<NIBPResponse>> interceptor = new NIBPBytesInterceptor();
    Assert.assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES, () -> {
      List<NIBPResponse> frames = interceptor.apply(ByteBuffer.wrap(input)).collect(Collectors.toList());
      Assert.assertEquals(frames.stream().flatMapToInt(NIBPResponse::extractPressure).toArray(), expected);
    }, logRecord -> Assert.assertEquals(logRecord.getMessage().replaceAll(".*" + NIBPResponse.class.getSimpleName(), Strings.EMPTY),
        "[ 0x3e, 0x05, 0x02, 0x01, 0xba ] 5 bytes")), logFlag);
  }
}