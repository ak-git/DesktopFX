package com.ak.comm.file;

import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.AbstractConverter;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class AutoFileReadingServiceTest {
  private enum TestVariables implements Variable<TestVariables> {
    VAR
  }

  private static final Converter<BufferFrame, TestVariables> INTEGER_CONVERTER =
      new AbstractConverter<BufferFrame, TestVariables>(TestVariables.class) {
        @Override
        protected Stream<int[]> innerApply(@Nonnull BufferFrame bufferFrame) {
          return Stream.of(new int[] {0});
        }
      };

  @Test(timeOut = 10000, dataProviderClass = FileDataProvider.class, dataProvider = "files")
  public void testDefaultBytesInterceptor(@Nonnull Path fileToRead, @Nonnegative int bytes) throws Exception {
    FileFilter service = new AutoFileReadingService<>(new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 1),
        INTEGER_CONVERTER);

    Assert.assertEquals(service.accept(fileToRead.toFile()), bytes >= 0);
    Assert.assertEquals(service.accept(fileToRead.toFile()), bytes >= 0);
  }

  @Test
  public void testInvalidFile() {
    AutoFileReadingService<BufferFrame, BufferFrame, TestVariables> fileReadingService = new AutoFileReadingService<>(
        new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_921600, 2), INTEGER_CONVERTER);
    Assert.assertFalse(fileReadingService.accept(Paths.get(Strings.EMPTY).toFile()));
    fileReadingService.cancel();
    Assert.assertThrows(UnsupportedOperationException.class, () -> fileReadingService.subscribe(null));
  }
}