package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.comm.logging.LogTestUtils;
import com.ak.logging.LogBuilders;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConcurrentAsyncFileChannelTest {
  private static final Logger LOGGER = Logger.getLogger(ConcurrentAsyncFileChannel.class.getName());

  @Test
  public void testWriteAndRead() {
    ConcurrentAsyncFileChannel channel = new ConcurrentAsyncFileChannel(() -> {
      Path path = LogBuilders.TIME.build(ConcurrentAsyncFileChannelTest.class.getSimpleName()).getPath();
      return AsynchronousFileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.READ);
    });

    channel.close();
    ByteBuffer byteBuffer = ByteBuffer.allocate(1);
    byteBuffer.put((byte) 1);
    byteBuffer.flip();

    for (int i = 0; i < 10; i++) {
      channel.write(byteBuffer);
      byteBuffer.clear();
      channel.read(byteBuffer, 0);
      Assert.assertEquals(byteBuffer.array()[0], 1);
      byteBuffer.rewind();
    }
    channel.close();
  }

  @Test
  public void testParallelWriteAndRead() throws InterruptedException, ExecutionException {
    ConcurrentAsyncFileChannel channel = new ConcurrentAsyncFileChannel(() -> {
      Path path = LogBuilders.TIME.build(ConcurrentAsyncFileChannelTest.class.getSimpleName() + "Parallel").getPath();
      return AsynchronousFileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.READ);
    });
    ByteBuffer buffer = ByteBuffer.allocate(4);
    channel.read(buffer, 100);
    Assert.assertEquals(buffer.position(), 0);
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    int INTS = 1024;
    Future<?> writeFuture = executorService.submit(() -> {
      ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);

      for (int i = 0; i < INTS; i++) {
        byteBuffer.clear();
        byteBuffer.putInt(i);
        byteBuffer.flip();
        channel.write(byteBuffer);
      }
    });

    Future<?> readFuture = executorService.submit(() -> {
      ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES * INTS);
      for (int i = 0; i < INTS; i++) {
        byteBuffer.clear();
        channel.read(byteBuffer, 0);
        byteBuffer.flip();
        for (int j = 0; byteBuffer.hasRemaining(); j++) {
          Assert.assertEquals(byteBuffer.getInt(), j);
        }
      }
    });

    writeFuture.get();
    readFuture.get();
    channel.close();
  }

  @Test
  public void testInvalidInitialize() {
    Assert.assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, Level.WARNING, () -> {
      ConcurrentAsyncFileChannel channel = new ConcurrentAsyncFileChannel(() -> {
        throw new Exception(ConcurrentAsyncFileChannel.class.getSimpleName());
      });
      channel.write(ByteBuffer.allocate(1));
      channel.read(ByteBuffer.allocate(1), 1);
    }, logRecord -> {
      Assert.assertTrue(logRecord.getMessage().contains(ConcurrentAsyncFileChannel.class.getSimpleName()));
      Assert.assertEquals(logRecord.getThrown().getClass().getSimpleName(), Exception.class.getSimpleName());
    }));
  }

  @Test
  public void testNullInitialize() {
    ConcurrentAsyncFileChannel channel = new ConcurrentAsyncFileChannel(() -> null);
    ByteBuffer buffer = ByteBuffer.allocate(1);
    channel.write(buffer);
    buffer.clear();
    channel.read(buffer, 1);
    Assert.assertEquals(buffer.position(), 0);
  }
}