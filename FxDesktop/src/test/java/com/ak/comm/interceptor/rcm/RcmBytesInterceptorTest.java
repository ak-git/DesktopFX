package com.ak.comm.interceptor.rcm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.logging.LogTestUtils;
import com.ak.util.LogUtils;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RcmBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(RcmBytesInterceptor.class.getName());
  private final BytesInterceptor<BufferFrame, BufferFrame> interceptor = new RcmBytesInterceptor();
  private final ByteBuffer buffer = ByteBuffer.allocate(100);

  private RcmBytesInterceptorTest() {
  }

  @DataProvider(name = "rcm-data")
  public static Object[][] data() {
    return new Object[][] {
        {
            new byte[] {
                (byte) 0xf7, (byte) 0xf8, (byte) 0x81, (byte) 0x80, (byte) 0xfb, (byte) 0xc0, (byte) 0x81, (byte) 0xe8, (byte) 0x81, (byte) 0x80, //  invalid data
                -10, -36, -125, -72, -5, -60, -125, -124, -111, -94, -7, -98, -127, -128, -5, -78, -127, -10, -127, -128,
                -10,
            },
            new BufferFrame(new byte[] {
                (byte) 0xf6, (byte) 0xdc, (byte) 0x83, (byte) 0xb8, (byte) 0xfb, (byte) 0xc4, (byte) 0x83, (byte) 0x84,
                (byte) 0x91, (byte) 0xa2, (byte) 0xf9, (byte) 0x9e, (byte) 0x81, (byte) 0x80, (byte) 0xfb, (byte) 0xb2, (byte) 0x81, (byte) 0xf6, (byte) 0x81, (byte) 0x80
            }, ByteOrder.LITTLE_ENDIAN),
            "[ 0xf7, 0xf8, 0x81, 0x80, 0xfb, 0xc0, 0x81, 0xe8, 0x81, 0x80 ] 10 bytes IGNORED"
        }
    };
  }

  @Test(dataProvider = "rcm-data")
  public void testInterceptor(@Nonnull byte[] bytes, @Nullable BufferFrame response, @Nonnull String ignoredMessage) {
    buffer.clear();
    buffer.put(bytes);
    buffer.flip();

    Assert.assertEquals(interceptor.getBaudRate(), 115200 / 3);
    Assert.assertNull(interceptor.getPingRequest());
    Assert.assertEquals(interceptor.getSerialParams(), EnumSet.of(BytesInterceptor.SerialParams.CLEAR_DTR));

    AtomicReference<String> logMessage = new AtomicReference<>(Strings.EMPTY);
    Assert.assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
        () -> {
          Stream<BufferFrame> frames = interceptor.apply(buffer);
          Assert.assertEquals(frames.iterator(), Collections.singleton(response).iterator());
          Assert.assertTrue(logMessage.get().endsWith(ignoredMessage), logMessage.get());
        },
        logRecord -> logMessage.set(logRecord.getMessage())
    ));
  }
}
