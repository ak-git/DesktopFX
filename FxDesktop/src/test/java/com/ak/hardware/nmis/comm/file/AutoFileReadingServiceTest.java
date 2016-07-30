package com.ak.hardware.nmis.comm.file;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;

import com.ak.comm.file.AutoFileReadingService;
import com.ak.comm.interceptor.DefaultBytesInterceptor;
import com.ak.hardware.nmis.comm.interceptor.NmisBytesInterceptor;
import com.ak.hardware.nmis.comm.interceptor.NmisProtocolByte;
import com.ak.hardware.nmis.comm.interceptor.NmisRequest;
import com.ak.hardware.nmis.comm.interceptor.NmisResponseFrame;
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

  @Test(timeOut = 10000)
  public void testBytesInterceptor() throws Exception {
    AutoFileReadingService<NmisResponseFrame, NmisRequest> service = new AutoFileReadingService<>(new NmisBytesInterceptor());
    TestSubscriber<NmisResponseFrame> subscriber = TestSubscriber.create();
    service.getBufferObservable().subscribe(subscriber);

    Path path = new BinaryLogBuilder(getClass().getSimpleName(), LocalFileHandler.class).build().getPath();
    try (WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer = ByteBuffer.allocate(NmisProtocolByte.MAX_CAPACITY);
      for (NmisRequest.Sequence sequence : NmisRequest.Sequence.values()) {
        buffer.clear();
        sequence.build().writeTo(buffer);
        buffer.flip();
        channel.write(buffer);
      }
    }


    int eventCount = NmisRequest.Sequence.values().length;
    CountDownLatch latch = new CountDownLatch(eventCount);
    service.getBufferObservable().subscribe(response -> latch.countDown());

    Assert.assertTrue(service.accept(path.toFile()));
    latch.await();
    subscriber.assertNotCompleted();
    service.close();
    subscriber.assertValueCount(eventCount);
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
  }

  @Test
  public void testInvalidFile() {
    Assert.assertFalse(new AutoFileReadingService<>(new DefaultBytesInterceptor()).accept(Paths.get("").toFile()));
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