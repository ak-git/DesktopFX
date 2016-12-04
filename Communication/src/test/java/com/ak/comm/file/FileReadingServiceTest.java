package com.ak.comm.file;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class FileReadingServiceTest {
  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "files")
  public void testFile(@Nonnull Path fileToRead, @Nonnegative int bytes) throws Exception {
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
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "files")
  public void testCancel(@Nonnull Path fileToRead, @Nonnegative int bytes) {
    TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create();
    Publisher<ByteBuffer> publisher = new FileReadingService(fileToRead);
    Flowable.fromPublisher(publisher).doOnSubscribe(Subscription::cancel).subscribe(testSubscriber);
    testSubscriber.assertNoErrors();
    testSubscriber.assertNoValues();
    if (bytes < 0) {
      testSubscriber.assertNotSubscribed();
      testSubscriber.assertNotComplete();
    }
    else {
      testSubscriber.assertSubscribed();
      testSubscriber.assertComplete();
    }
  }
}