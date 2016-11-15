package com.ak.hardware.simple.comm.file;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;

import com.ak.comm.file.AutoFileReadingService;
import com.ak.hardware.simple.comm.interceptor.DefaultBytesInterceptor;
import com.ak.logging.BinaryLogBuilder;
import com.ak.logging.LocalFileHandler;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

public final class AutoFileReadingServiceTest {
  @Test(timeOut = 10000)
  public void testDefaultBytesInterceptor() throws Exception {
    AutoFileReadingService<Integer, Byte> service = new AutoFileReadingService<>(new DefaultBytesInterceptor());
    TestSubscriber<Integer> subscriber = TestSubscriber.create();
    service.getBufferObservable().subscribe(subscriber);
    subscriber.assertNotCompleted();

    Path path = new BinaryLogBuilder(getClass().getSimpleName(), LocalFileHandler.class).build().getPath();
    try (WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer = ByteBuffer.wrap(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
      for (int i = 0; i < 1024; i++) {
        channel.write(buffer);
        buffer.rewind();
      }
    }

    int eventCount = 1024 * 10;
    CountDownLatch latch = new CountDownLatch(eventCount);
    service.getBufferObservable().subscribe(response -> latch.countDown());

    Assert.assertTrue(service.accept(path.toFile()));
    Assert.assertTrue(service.accept(path.toFile()));
    latch.await();
    Assert.assertTrue(service.accept(path.toFile()));
    subscriber.assertNotCompleted();
    service.close();
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
  }

  @Test
  public void testInvalidFile() {
    AutoFileReadingService<Integer, Byte> fileReadingService = new AutoFileReadingService<>(new DefaultBytesInterceptor());
    Assert.assertFalse(fileReadingService.accept(Paths.get(Strings.EMPTY).toFile()));
    Assert.assertFalse(fileReadingService.isOpen());
  }
}