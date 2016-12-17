package com.ak.comm.file;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.ak.comm.core.LogLevelSubstitution;
import com.ak.comm.core.LogLevels;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.DefaultBytesInterceptor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static jssc.SerialPort.BAUDRATE_115200;

public final class DefaultBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(DefaultBytesInterceptor.class.getName());

  @Test
  public void testInterceptorProperties() {
    BytesInterceptor<Integer, Byte> interceptor = new DefaultBytesInterceptor(BytesInterceptor.BaudRate.BR_115200);
    Assert.assertEquals(interceptor.getBaudRate(), BAUDRATE_115200);
    Assert.assertEquals(interceptor.getPingRequest(), Byte.valueOf((byte) 0));
  }

  @DataProvider(name = "responses")
  public static Object[][] responses() {
    return new Object[][] {
        {new byte[] {0x00}, 0},
        {new byte[] {0x71}, 113},
        {new byte[] {(byte) 0xff}, 255},
    };
  }

  @Test(dataProvider = "responses")
  public void testDefaultBytesInterceptor(byte[] input, Integer response) {
    BytesInterceptor<Integer, Byte> interceptor = new DefaultBytesInterceptor(BytesInterceptor.BaudRate.BR_921600);

    LogLevelSubstitution.substituteLogLevel(LOGGER, LogLevels.LOG_LEVEL_LEXEMES,
        () -> {
          Collection<Integer> frames = interceptor.apply(ByteBuffer.wrap(input)).collect(Collectors.toList());
          Assert.assertEquals(frames, Collections.singleton(response));
        },
        logRecord -> Assert.assertTrue(logRecord.getMessage().endsWith(response.toString()), logRecord.getMessage()));

    LogLevelSubstitution.substituteLogLevel(LOGGER, LogLevels.LOG_LEVEL_LEXEMES,
        () -> Assert.assertTrue(interceptor.putOut(response.byteValue()).remaining() > 0),
        logRecord -> Assert.assertTrue(logRecord.getMessage().endsWith(response.byteValue() + " - OUT to hardware"), logRecord.getMessage()));
  }
}