package com.ak.comm.file;

import java.io.FileFilter;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class AutoFileReadingServiceTest {
  private final FileFilter service = new AutoFileReadingService<>(
      () -> new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 1 + TwoVariables.values().length * Integer.BYTES),
      () -> new ToIntegerConverter<>(TwoVariables.class));


  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "parallelRampFiles", invocationCount = 10)
  public void testAccept(@Nonnull Path file) {
    Assert.assertTrue(service.accept(file.toFile()));
  }
}