package com.ak.comm.interceptor.simple;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.BytesInterceptor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StringBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(StringBytesInterceptor.class.getName());
  private final Function<ByteBuffer, Stream<String>> interceptor = new StringBytesInterceptor(getClass().getName());

  @Test
  public void testInterceptorProperties() {
    BytesInterceptor<BufferFrame, String> interceptor = new StringBytesInterceptor(getClass().getName());
    Assert.assertEquals(interceptor.getBaudRate(), 115200);
    Assert.assertNull(interceptor.getPingRequest());
  }

  @Test(dataProvider = "string-data")
  public void testInterceptor(@Nonnull byte[] bytes, @Nullable String response, @Nonnull String ignoredMessage) {
    new FrameBytesInterceptorDataProvider().testInterceptor(bytes, response, ignoredMessage, LOGGER, interceptor);
  }

  @DataProvider(name = "string-data")
  public static Object[][] data() {
    return new Object[][] {
        {
            new byte[] {
                '1', '2', '3', '4', '5', '6', 0x37, 0x38, 0x39, 0x30,
                51, 102, 102, 53, '\r', '\n'
            },
            "3ff5",
            "[ 0x37, 0x38, 0x39, 0x30 ] 4 bytes IGNORED"
        },
    };
  }
}