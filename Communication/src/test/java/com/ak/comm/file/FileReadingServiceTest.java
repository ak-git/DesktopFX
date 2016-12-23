package com.ak.comm.file;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.util.LogUtils;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class FileReadingServiceTest {
  private static final Logger LOGGER = Logger.getLogger(FileReadingService.class.getName());

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "files")
  public void testFile(@Nonnull Path fileToRead, @Nonnegative int bytes) {
    TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create();
    Publisher<ByteBuffer> publisher = new FileReadingService(fileToRead);
    Assert.assertTrue(publisher.toString().contains(fileToRead.toString()));
    Flowable.fromPublisher(publisher).subscribe(testSubscriber);

    testSubscriber.assertNoErrors();
    if (bytes < 0) {
      testSubscriber.assertNoValues();
      testSubscriber.assertNotSubscribed();
      testSubscriber.assertNotComplete();
    }
    else {
      testSubscriber.assertValueCount((int) Math.ceil(bytes / 1024.0 / 4.0));
      testSubscriber.assertSubscribed();
      testSubscriber.assertComplete();
    }
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "filesCanDelete")
  public void testException(@Nonnull Path fileToRead, @Nonnegative int bytes) {
    LogUtils.substituteLogLevel(LOGGER, Level.WARNING, () -> {
      TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create();
      Publisher<ByteBuffer> publisher = new FileReadingService(fileToRead);
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

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "files")
  public void testCancel(@Nonnull Path fileToRead, @Nonnegative int bytes) {
    TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create();
    Publisher<ByteBuffer> publisher = new FileReadingService(fileToRead);
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

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "files")
  public void testLogBytes(@Nonnull Path fileToRead, @Nonnegative int bytes) {
    LogUtils.substituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_BYTES, () -> {
      Publisher<ByteBuffer> publisher = new FileReadingService(fileToRead);
      Flowable.fromPublisher(publisher).subscribe();
    }, new Consumer<LogRecord>() {
      private static final int CAPACITY_4K = 4096;
      int packCounter;

      @Override
      public void accept(LogRecord logRecord) {
        int bytesCount = (bytes - packCounter * CAPACITY_4K) >= CAPACITY_4K ? CAPACITY_4K : bytes % CAPACITY_4K;
        Assert.assertTrue(logRecord.getMessage().endsWith(bytesCount + " bytes IN from hardware"), logRecord.getMessage());
        packCounter++;
      }
    });
  }
}