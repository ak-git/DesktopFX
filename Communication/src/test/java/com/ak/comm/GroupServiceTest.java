package com.ak.comm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.file.FileDataProvider;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class GroupServiceTest {
  private final GroupService<BufferFrame, BufferFrame, TwoVariables> service = new GroupService<>(
      () -> new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 1 + TwoVariables.values().length * Integer.BYTES),
      () -> new ToIntegerConverter<>(TwoVariables.class, 1000));


  private GroupServiceTest() {
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "parallelRampFiles", invocationCount = 10)
  public void testRead(@Nonnull Path file) {
    Assert.assertTrue(service.accept(file.toFile()));
    while (!Thread.currentThread().isInterrupted()) {
      int countFrames = 10;
      int shift = 2;
      List<int[]> ints = service.read(shift, countFrames + shift);
      if (!ints.isEmpty()) {
        for (int i = 0; i < countFrames; i++) {
          for (int j = 0; j < TwoVariables.values().length; j++) {
            Assert.assertEquals(ints.get(j)[i], i + j + shift, Arrays.toString(ints.get(j)));
          }
        }
        break;
      }
    }
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "parallelRampFiles", invocationCount = 10)
  public void testNotRead(@Nonnull Path file) {
    Assert.assertTrue(service.accept(file.toFile()));
    List<int[]> ints = service.read(1, 1);
    Assert.assertTrue(ints.isEmpty());
  }

  @AfterClass
  public void tearDown() throws IOException {
    service.close();
  }
}