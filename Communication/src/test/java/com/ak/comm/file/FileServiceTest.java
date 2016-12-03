package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.logging.BinaryLogBuilder;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class FileServiceTest {
  private static final int KILO_BYTE = 1024;

  @DataProvider(name = "files")
  public Object[][] simple() throws IOException {
    return new Object[][] {
        {createFile(-1), -1},
        {createFile(0), 0},
        {createFile(14), 14 / 4 + 1}
    };
  }

  @Test(dataProvider = "files")
  public void testFile(@Nonnull Path fileToRead, @Nonnegative int valueCount) throws Exception {
    TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create();
    Publisher<ByteBuffer> publisher = new FileService(fileToRead);
    Assert.assertTrue(publisher.toString().contains(fileToRead.toString()));
    Flowable.fromPublisher(publisher).subscribe(testSubscriber);

    testSubscriber.assertNoErrors();
    if (valueCount < 0) {
      testSubscriber.assertNoValues();
      testSubscriber.assertNotSubscribed();
      testSubscriber.assertNotComplete();
    }
    else {
      testSubscriber.assertValueCount(valueCount);
      testSubscriber.assertSubscribed();
      testSubscriber.assertComplete();
    }
    Files.deleteIfExists(fileToRead);
  }

  @Test(dataProvider = "files")
  public void testException(@Nonnull Path fileToRead, @Nonnegative int valueCount) {
    TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create();
    Publisher<ByteBuffer> publisher = new FileService(fileToRead);
    Flowable.fromPublisher(publisher).doOnSubscribe(subscription -> Files.deleteIfExists(fileToRead)).subscribe(testSubscriber);
    if (valueCount < 0) {
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

  @Test(dataProvider = "files")
  public void testCancel(@Nonnull Path fileToRead, @Nonnegative int valueCount) throws IOException {
    TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create();
    Publisher<ByteBuffer> publisher = new FileService(fileToRead);
    Flowable.fromPublisher(publisher).doOnSubscribe(Subscription::cancel).subscribe(testSubscriber);
    testSubscriber.assertNoErrors();
    testSubscriber.assertNoValues();
    if (valueCount < 0) {
      testSubscriber.assertNotSubscribed();
      testSubscriber.assertNotComplete();
    }
    else {
      testSubscriber.assertSubscribed();
      testSubscriber.assertComplete();
    }
    Files.deleteIfExists(fileToRead);
  }

  private Path createFile(int kBytes) throws IOException {
    Path path = new BinaryLogBuilder(String.format("%s %d bytes", getClass().getSimpleName(), kBytes)).build().getPath();
    if (kBytes >= 0) {
      try (WritableByteChannel channel = Files.newByteChannel(path,
          StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
        ByteBuffer buffer = ByteBuffer.allocate(KILO_BYTE);
        for (int i = 0; i < KILO_BYTE; i++) {
          buffer.put((byte) i);
        }
        for (int i = 0; i < kBytes; i++) {
          buffer.rewind();
          channel.write(buffer);
        }
      }
    }
    return path;
  }
}