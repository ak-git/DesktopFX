package com.ak.comm.interceptor.simple;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.ak.comm.file.FilePublisher;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.BinaryLogBuilder;
import com.ak.logging.LocalFileHandler;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.Assert;
import org.testng.annotations.Test;

import static jssc.SerialPort.BAUDRATE_115200;

public final class DefaultBytesInterceptorTest {
  @Test
  public void testDefaultBytesInterceptor() throws Exception {
    Path path = new BinaryLogBuilder(getClass().getSimpleName(), LocalFileHandler.class).build().getPath();
    try (WritableByteChannel channel = Files.newByteChannel(path,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer = ByteBuffer.wrap(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
      for (int i = 0; i < 1024; i++) {
        channel.write(buffer);
        buffer.rewind();
      }
    }

    TestSubscriber<Integer> testSubscriber = TestSubscriber.create();
    BytesInterceptor<Integer, Byte> interceptor = new DefaultBytesInterceptor();
    interceptor.putOut((byte) 1);
    Assert.assertEquals(interceptor.name(), "None");
    Assert.assertEquals(interceptor.getBaudRate(), BAUDRATE_115200);
    Assert.assertEquals(interceptor.getPingRequest(), Byte.valueOf((byte) 0));

    Flowable.fromPublisher(new FilePublisher(path)).flatMap(interceptor).subscribe(testSubscriber);
    testSubscriber.assertNoErrors();
    testSubscriber.assertValueCount(1024 * 10);
    testSubscriber.assertSubscribed();
    testSubscriber.assertComplete();
    Files.deleteIfExists(path);
  }
}