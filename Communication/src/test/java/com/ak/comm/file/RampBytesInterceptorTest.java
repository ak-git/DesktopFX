package com.ak.comm.file;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.core.LogLevelSubstitution;
import com.ak.comm.core.LogLevels;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static jssc.SerialPort.BAUDRATE_115200;

public final class RampBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(RampBytesInterceptor.class.getName());

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidInterceptorProperties() {
    new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 0);
  }

  @Test
  public void testInterceptorProperties() {
    BytesInterceptor<BufferFrame, BufferFrame> interceptor = new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 1);
    Assert.assertEquals(interceptor.getBaudRate(), BAUDRATE_115200);
    Assert.assertNull(interceptor.getPingRequest());
  }

  @DataProvider(name = "responses")
  public static Object[][] responses() {
    return new Object[][] {
        {
            // invalid first byte, 0x00 at start
            new byte[] {0x00,
                (byte) 255, (byte) 0xe0, (byte) 0xff, 0x3f, 0x00, (byte) 0xea, (byte) 0xff, 0x3f, 0x00,
                0, (byte) 0xe0, (byte) 0xff, 0x3f, 0x00, (byte) 0xea, (byte) 0xff, 0x3f, 0x00
            },
            new BufferFrame(new byte[] {
                (byte) 255, (byte) 0xe0, (byte) 0xff, 0x3f, 0x00, (byte) 0xea, (byte) 0xff, 0x3f, 0x00
            })
        },
        {
            new byte[] {
                0x7f, (byte) 0xe0, (byte) 0xff, 0x3f, 0x00, (byte) 0xea, (byte) 0xff, 0x3f, 0x00,
                (byte) 0x80, (byte) 0xe0, (byte) 0xff, 0x3f, 0x00, (byte) 0xea, (byte) 0xff, 0x3f, 0x00
            },
            new BufferFrame(new byte[] {
                (byte) 127, (byte) 0xe0, (byte) 0xff, 0x3f, 0x00, (byte) 0xea, (byte) 0xff, 0x3f, 0x00
            })
        },
    };
  }

  @Test(dataProvider = "responses")
  public void testDefaultBytesInterceptor(byte[] input, BufferFrame testFrame) {
    BytesInterceptor<BufferFrame, BufferFrame> interceptor = new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_921600, 9);

    LogLevelSubstitution.substituteLogLevel(LOGGER, LogLevels.LOG_LEVEL_LEXEMES,
        () -> {
          Collection<BufferFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).collect(Collectors.toList());
          Assert.assertEquals(frames, Collections.singleton(testFrame));
        },
        logRecord -> Assert.assertTrue(logRecord.getMessage().endsWith(testFrame.toString()), logRecord.getMessage()));

    AtomicReference<String> logMessage = new AtomicReference<>("");
    LogLevelSubstitution.substituteLogLevel(LOGGER, LogLevels.LOG_LEVEL_LEXEMES,
        () -> {
          int bytesOut = interceptor.putOut(testFrame).remaining();
          Assert.assertTrue(bytesOut > 0);
          Assert.assertEquals(logMessage.get(),
              testFrame.toString().replaceAll(".*" + BufferFrame.class.getSimpleName(), "") +
                  " - " + bytesOut + " bytes OUT to hardware");
        },
        logRecord -> logMessage.set(logRecord.getMessage().replaceAll(".*" + BufferFrame.class.getSimpleName(), "")));
  }
}