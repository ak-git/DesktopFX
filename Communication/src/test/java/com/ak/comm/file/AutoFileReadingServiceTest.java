package com.ak.comm.file;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class AutoFileReadingServiceTest {
  @Test(timeOut = 10000, dataProviderClass = FileDataProvider.class, dataProvider = "files")
  public void testRampBytesInterceptor(@Nonnull Path fileToRead, @Nonnegative int bytes) throws InterruptedException {
    AutoFileReadingService<BufferFrame, BufferFrame, TwoVariables> service = new AutoFileReadingService<>(
        new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 1 + TwoVariables.values().length * Integer.BYTES),
        new ToIntegerConverter<>(TwoVariables.class));

    CountDownLatch latch = new CountDownLatch(1);
    service.subscribe(new SingleObserver<Path>() {
      @Override
      public void onSubscribe(Disposable d) {
        if (bytes < 0) {
          Assert.fail(fileToRead.toString());
        }
      }

      @Override
      public void onSuccess(Path value) {
        if (bytes > 0) {
          Assert.assertTrue(value.toString().contains(AutoFileReadingService.class.getSimpleName()));
        }
        else {
          Assert.assertNull(value);
        }
        latch.countDown();
      }

      @Override
      public void onError(Throwable e) {
        Assert.fail(fileToRead.toString(), e);
      }
    });
    Assert.assertEquals(service.accept(fileToRead.toFile()), bytes >= 0);
    if (bytes >= 0) {
      latch.await();
    }
  }
}