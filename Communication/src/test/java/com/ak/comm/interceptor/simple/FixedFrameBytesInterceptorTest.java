package com.ak.comm.interceptor.simple;

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
import com.ak.comm.logging.LogTestUtils;
import com.ak.util.LogUtils;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FixedFrameBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(FixedFrameBytesInterceptor.class.getName());
  private final Function<ByteBuffer, Stream<BufferFrame>> interceptor =
      new FixedFrameBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 9);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidInterceptorProperties() {
    new FixedFrameBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 0);
  }

  @Test
  public void testInterceptorProperties() {
    BytesInterceptor<BufferFrame, BufferFrame> interceptor = new FixedFrameBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 1);
    Assert.assertEquals(interceptor.getBaudRate(), 115200);
    Assert.assertNull(interceptor.getPingRequest());
  }

  @Test(dataProviderClass = FrameBytesInterceptorDataProvider.class, dataProvider = "fixed-start-data")
  public void testFixedBytesInterceptor(@Nonnull byte[] input, @Nonnull BufferFrame testFrame, @Nonnull String ignoredMessage) {
    BytesInterceptor<BufferFrame, BufferFrame> interceptor = new FixedFrameBytesInterceptor(BytesInterceptor.BaudRate.BR_921600, 9);

    Assert.assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES,
        () -> {
          Collection<BufferFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).collect(Collectors.toList());
          Assert.assertEquals(frames, Collections.singleton(testFrame));
        },
        logRecord -> Assert.assertTrue(logRecord.getMessage().endsWith(testFrame.toString()),
            String.format("[ %s ] must ends with [ %s ]", logRecord.getMessage(), testFrame))));

    AtomicReference<String> logMessage = new AtomicReference<>(Strings.EMPTY);
    Assert.assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
        () -> {
          int bytesOut = interceptor.putOut(testFrame).remaining();
          Assert.assertTrue(bytesOut > 0);
          Assert.assertEquals(logMessage.get(),
              testFrame.toString().replaceAll(".*" + BufferFrame.class.getSimpleName(), Strings.EMPTY) +
                  " - " + bytesOut + " bytes OUT to hardware");
        },
        logRecord -> logMessage.set(logRecord.getMessage().replaceAll(".*" + BufferFrame.class.getSimpleName(), Strings.EMPTY))));

    BufferFrame singleByte = new BufferFrame(new byte[] {input[0]}, ByteOrder.nativeOrder());
    Assert.assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
        () -> Assert.assertTrue(interceptor.putOut(singleByte).remaining() > 0),
        logRecord -> Assert.assertTrue(logRecord.getMessage().endsWith(singleByte + " - OUT to hardware"), logRecord.getMessage())));

    Assert.assertFalse(ignoredMessage.isEmpty());
  }

  @Test(dataProviderClass = FrameBytesInterceptorDataProvider.class, dataProvider = "fixed-start-data")
  public void testInterceptor(@Nonnull byte[] bytes, @Nullable BufferFrame response, @Nonnull String ignoredMessage) {
    new FrameBytesInterceptorDataProvider().testInterceptor(bytes, response, ignoredMessage, LOGGER, interceptor);
  }
}