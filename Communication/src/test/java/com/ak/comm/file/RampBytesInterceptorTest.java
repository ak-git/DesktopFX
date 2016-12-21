package com.ak.comm.file;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.comm.util.LogUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static jssc.SerialPort.BAUDRATE_115200;

public final class RampBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(RampBytesInterceptor.class.getName());
  private final Function<ByteBuffer, Stream<BufferFrame>> interceptor =
      new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 9);
  private final ByteBuffer byteBuffer = ByteBuffer.allocate(20);

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

  @DataProvider(name = "data")
  public static Object[][] data() {
    return new Object[][] {
        {
            // invalid first byte, 0x00 at start
            new byte[] {0x7f,
                (byte) 255, 4, 3, 2, 1, 4, 3, 2, 1,
                0, 9, 10, 11, 12, 13, 14, 15, 16
            },
            new BufferFrame(new byte[] {
                (byte) 255, 4, 3, 2, 1, 4, 3, 2, 1
            }, ByteOrder.nativeOrder())
        },
        {
            // check 127, -128 ramp step
            new byte[] {
                0x7f, 17, 18, 19, 20, 21, 22, 23, 24,
                (byte) 0x80, 25, 26, 27, 28, 29, 30, 31, 32
            },
            new BufferFrame(new byte[] {
                (byte) 127, 17, 18, 19, 20, 21, 22, 23, 24
            }, ByteOrder.nativeOrder())
        }
    };
  }

  @Test(dataProvider = "data")
  public void testRampBytesInterceptor(byte[] input, BufferFrame testFrame) {
    BytesInterceptor<BufferFrame, BufferFrame> interceptor = new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_921600, 9);

    LogUtils.substituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES,
        () -> {
          Collection<BufferFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).collect(Collectors.toList());
          Assert.assertEquals(frames, Collections.singleton(testFrame));
        },
        logRecord -> Assert.assertTrue(logRecord.getMessage().endsWith(testFrame.toString()), logRecord.getMessage()));

    AtomicReference<String> logMessage = new AtomicReference<>("");
    LogUtils.substituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES,
        () -> {
          int bytesOut = interceptor.putOut(testFrame).remaining();
          Assert.assertTrue(bytesOut > 0);
          Assert.assertEquals(logMessage.get(),
              testFrame.toString().replaceAll(".*" + BufferFrame.class.getSimpleName(), "") +
                  " - " + bytesOut + " bytes OUT to hardware");
        },
        logRecord -> logMessage.set(logRecord.getMessage().replaceAll(".*" + BufferFrame.class.getSimpleName(), "")));

    BufferFrame singleByte = new BufferFrame(new byte[] {input[0]}, ByteOrder.nativeOrder());
    LogUtils.substituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES,
        () -> Assert.assertTrue(interceptor.putOut(singleByte).remaining() > 0),
        logRecord -> Assert.assertTrue(logRecord.getMessage().endsWith(singleByte + " - OUT to hardware"), logRecord.getMessage()));
  }

  @Test(dataProvider = "data")
  public void testInterceptor(@Nonnull byte[] bytes, @Nullable BufferFrame response) {
    byteBuffer.clear();
    byteBuffer.put(bytes);
    byteBuffer.flip();

    AtomicReference<String> logMessage = new AtomicReference<>("");
    LogUtils.substituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
        () -> {
          Stream<BufferFrame> frames = interceptor.apply(byteBuffer);
          Assert.assertEquals(frames.iterator(), Collections.singleton(response).iterator());
          Assert.assertTrue(logMessage.get().endsWith(" IGNORED"));
        },
        logRecord -> logMessage.set(logRecord.getMessage())
    );
  }
}