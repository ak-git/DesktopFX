package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;

import com.ak.comm.core.Service;
import com.ak.logging.BinaryLogBuilder;
import com.ak.logging.LocalFileHandler;
import com.ak.logging.LogPathBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import rx.Observer;
import rx.observers.TestSubscriber;

public final class FileReadingServiceTest {
  @Test
  public void testInvalidFile() throws IOException {
    Path path = new BinaryLogBuilder(getClass().getSimpleName(), LocalFileHandler.class).build().getPath();
    TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create();
    Service<ByteBuffer> service = new FileReadingService(path, testSubscriber);
    Assert.assertTrue(service.toString().contains(path.toString()));
    service.close();
    testSubscriber.assertNotCompleted();
    Files.deleteIfExists(path);
  }

  @Test(timeOut = 10000)
  public void testFile() throws Exception {
    Path path = new BinaryLogBuilder(getClass().getSimpleName(), LocalFileHandler.class).build().getPath();
    try (WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer = ByteBuffer.wrap(new byte[] {'a', 'b', 'c', '1', '2', '3', '\n'});
      for (int i = 0; i < 2048; i++) {
        channel.write(buffer);
        buffer.rewind();
      }
    }

    CountDownLatch latch = new CountDownLatch(4);
    TestSubscriber<ByteBuffer> testSubscriber = TestSubscriber.create(new Observer<ByteBuffer>() {
      @Override
      public void onCompleted() {
      }

      @Override
      public void onError(Throwable e) {
      }

      @Override
      public void onNext(ByteBuffer buffer) {
        latch.countDown();
      }
    });
    try (Service<ByteBuffer> ignored = new FileReadingService(path, testSubscriber)) {
      latch.await();
      testSubscriber.assertNoErrors();
      testSubscriber.assertValueCount(4);
      testSubscriber.assertCompleted();
    }
    Files.deleteIfExists(path);
  }

  @AfterClass
  public void tearDown() throws Exception {
    Path logPath = new LogPathBuilder().addPath(LocalFileHandler.class.getSimpleName()).build().getPath();
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(logPath)) {
      for (Path file : ds) {
        Files.delete(file);
      }
    }
    Files.delete(logPath);
  }
}