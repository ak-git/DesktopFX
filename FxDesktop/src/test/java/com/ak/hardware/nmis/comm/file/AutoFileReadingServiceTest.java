package com.ak.hardware.nmis.comm.file;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;

import com.ak.comm.file.AutoFileReadingService;
import com.ak.hardware.nmis.comm.interceptor.NmisBytesInterceptor;
import com.ak.hardware.nmis.comm.interceptor.NmisProtocolByte;
import com.ak.hardware.nmis.comm.interceptor.NmisRequest;
import com.ak.hardware.nmis.comm.interceptor.NmisResponseFrame;
import com.ak.logging.BinaryLogBuilder;
import com.ak.logging.LocalFileHandler;
import org.testng.Assert;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

public final class AutoFileReadingServiceTest {
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
}