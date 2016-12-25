package com.ak.comm.file;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.comm.util.LogUtils;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class FileReadingServiceTest {
  private static final Logger LOGGER = Logger.getLogger(FileReadingService.class.getName());

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "rampFiles")
  public void testFile(@Nonnull Path fileToRead, @Nonnegative int bytes) {
    TestSubscriber<int[]> testSubscriber = TestSubscriber.create();
    Publisher<int[]> publisher = new FileReadingService<>(fileToRead, new RampBytesInterceptor(
        BytesInterceptor.BaudRate.BR_921600, 1 + TwoVariables.values().length * Integer.BYTES),
        new ToIntegerConverter<>(TwoVariables.class));
    Assert.assertTrue(publisher.toString().contains(fileToRead.toString()));

    LogUtils.substituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_BYTES, () ->
        Flowable.fromPublisher(publisher).subscribe(testSubscriber), new Consumer<LogRecord>() {
      private static final int CAPACITY_4K = 4096;
      int packCounter;

      @Override
      public void accept(LogRecord logRecord) {
        int bytesCount = (bytes - packCounter * CAPACITY_4K) >= CAPACITY_4K ? CAPACITY_4K : bytes % CAPACITY_4K;
        Assert.assertTrue(logRecord.getMessage().endsWith(bytesCount + " bytes IN from hardware"), logRecord.getMessage());
        packCounter++;
      }
    });

    testSubscriber.assertNoErrors();
    if (bytes < 0) {
      testSubscriber.assertNoValues();
      testSubscriber.assertNotSubscribed();
      testSubscriber.assertNotComplete();
    }
    else {
      testSubscriber.assertValueCount(bytes > 0 ? bytes / 9 - 1 : 0);
      testSubscriber.assertSubscribed();
      testSubscriber.assertComplete();
    }
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "filesCanDelete")
  public void testException(@Nonnull Path fileToRead, @Nonnegative int bytes) {
    LogUtils.substituteLogLevel(LOGGER, Level.WARNING, () -> {
      TestSubscriber<int[]> testSubscriber = TestSubscriber.create();
      Publisher<int[]> publisher = new FileReadingService<>(fileToRead, new RampBytesInterceptor(
          BytesInterceptor.BaudRate.BR_921600, 1 + TwoVariables.values().length * Integer.BYTES),
          new ToIntegerConverter<>(TwoVariables.class));
      Flowable.fromPublisher(publisher).doOnSubscribe(subscription -> Files.deleteIfExists(fileToRead)).subscribe(testSubscriber);
      if (bytes < 0) {
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotSubscribed();
      }
      else {
        testSubscriber.assertError(NoSuchFileException.class);
        testSubscriber.assertSubscribed();
      }
      testSubscriber.assertNoValues();
      testSubscriber.assertNotComplete();
    }, logRecord -> Assert.assertEquals(logRecord.getMessage(), fileToRead.toString()));
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "rampFiles")
  public void testCancel(@Nonnull Path fileToRead, @Nonnegative int bytes) {
    TestSubscriber<int[]> testSubscriber = TestSubscriber.create();
    Publisher<int[]> publisher = new FileReadingService<>(fileToRead, new RampBytesInterceptor(
        BytesInterceptor.BaudRate.BR_921600, 1 + TwoVariables.values().length * Integer.BYTES),
        new ToIntegerConverter<>(TwoVariables.class));
    Flowable.fromPublisher(publisher).doOnSubscribe(Subscription::cancel).subscribe(testSubscriber);
    testSubscriber.assertNoErrors();
    testSubscriber.assertNoValues();
    testSubscriber.assertNotComplete();
    if (bytes < 0) {
      testSubscriber.assertNotSubscribed();
    }
    else {
      testSubscriber.assertSubscribed();
    }
  }
}