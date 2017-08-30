package com.ak.comm.file;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AutoFileReadingServiceTest {
  private final AutoFileReadingService<BufferFrame, BufferFrame, TwoVariables> service = new AutoFileReadingService<>(
      () -> new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 1 + TwoVariables.values().length * Integer.BYTES),
      () -> new ToIntegerConverter<>(TwoVariables.class, 1000));


  private AutoFileReadingServiceTest() {
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "parallelRampFiles", invocationCount = 10)
  public void testAccept(@Nonnull Path file) {
    Assert.assertTrue(service.accept(file.toFile()));
    int countFrames = 10;
    ByteBuffer buffer = ByteBuffer.allocate(TwoVariables.values().length * Integer.BYTES * countFrames);
    while (!Thread.currentThread().isInterrupted()) {
      buffer.clear();
      service.read(buffer, 0);
      buffer.flip();
      if (buffer.limit() == buffer.capacity()) {
        for (int i = 0; i < countFrames; i++) {
          for (int j = 0; j < TwoVariables.values().length; j++) {
            Assert.assertEquals(buffer.getInt(), i + j);
          }
        }
        break;
      }
    }
  }

  @Test
  public void testNotAccept() {
    Assert.assertFalse(service.accept(Paths.get(Strings.EMPTY).toFile()));
  }
}