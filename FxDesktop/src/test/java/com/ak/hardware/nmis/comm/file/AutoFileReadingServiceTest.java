package com.ak.hardware.nmis.comm.file;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;

import com.ak.comm.file.AutoFileReadingService;
import com.ak.comm.interceptor.DefaultBytesInterceptor;
import com.ak.hardware.nmis.comm.interceptor.TnmiBytesInterceptor;
import com.ak.hardware.nmis.comm.interceptor.TnmiProtocolByte;
import com.ak.hardware.nmis.comm.interceptor.TnmiRequest;
import com.ak.hardware.nmis.comm.interceptor.TnmiResponseFrame;
import com.ak.logging.BinaryLogBuilder;
import com.ak.logging.LocalFileHandler;
import com.ak.logging.LogPathBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
    service.getBufferObservable().subscribe(response -> {
      latch.countDown();
    });

    Assert.assertTrue(service.accept(path.toFile()));
    Assert.assertTrue(service.accept(path.toFile()));
    latch.await();
    Assert.assertTrue(service.accept(path.toFile()));
    subscriber.assertNotCompleted();
    service.close();
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
  }

  @Test(timeOut = 10000)
  public void testTnmiBytesInterceptor() throws Exception {
    AutoFileReadingService<TnmiResponseFrame, TnmiRequest> service = new AutoFileReadingService<>(new TnmiBytesInterceptor());
    TestSubscriber<TnmiResponseFrame> subscriber = TestSubscriber.create();
    service.getBufferObservable().subscribe(subscriber);

    Path path = new BinaryLogBuilder(getClass().getSimpleName(), LocalFileHandler.class).build().getPath();
    try (WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer = ByteBuffer.allocate(TnmiProtocolByte.MAX_CAPACITY);
      for (TnmiRequest.Sequence sequence : TnmiRequest.Sequence.values()) {
        buffer.clear();
        sequence.build().writeTo(buffer);
        buffer.flip();
        channel.write(buffer);
      }
    }


    int eventCount = TnmiRequest.Sequence.values().length;
    CountDownLatch latch = new CountDownLatch(eventCount);
    service.getBufferObservable().subscribe(response -> {
      latch.countDown();
    });

    Assert.assertTrue(service.accept(path.toFile()));
    latch.await();
    subscriber.assertNotCompleted();
    service.close();
    subscriber.assertValueCount(eventCount);
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
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