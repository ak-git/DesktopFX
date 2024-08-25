package com.ak.comm.core;

import com.ak.comm.logging.LogTestUtils;
import com.ak.logging.LogBuilders;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrentAsyncFileChannelTest {
  private static final Logger LOGGER = Logger.getLogger(ConcurrentAsyncFileChannel.class.getName());

  @Test
  void testWriteAndRead() {
    ConcurrentAsyncFileChannel channel = new ConcurrentAsyncFileChannel(() -> {
      Path path = LogBuilders.TIME.build(ConcurrentAsyncFileChannelTest.class.getSimpleName()).getPath();
      return Optional.of(AsynchronousFileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.READ));
    });

    channel.close();
    ByteBuffer byteBuffer = ByteBuffer.allocate(1);
    byteBuffer.put((byte) 1);
    byteBuffer.flip();

    for (int i = 0; i < 10; i++) {
      channel.write(byteBuffer);
      byteBuffer.clear();
      channel.read(byteBuffer, 0);
      assertThat(byteBuffer.array()[0]).isEqualTo((byte) 1);
      byteBuffer.rewind();
    }
    channel.close();
  }

  @Test
  void testParallelWriteAndRead() throws InterruptedException, ExecutionException {
    ConcurrentAsyncFileChannel channel = new ConcurrentAsyncFileChannel(() -> {
      Path path = LogBuilders.TIME.build(ConcurrentAsyncFileChannelTest.class.getSimpleName() + "Parallel").getPath();
      return Optional.of(AsynchronousFileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.READ));
    });
    ByteBuffer buffer = ByteBuffer.allocate(4);
    channel.read(buffer, 100);
    assertThat(buffer.position()).isZero();
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    int ints = 1024;
    Future<?> writeFuture = executorService.submit(() -> {
      ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);

      for (int i = 0; i < ints; i++) {
        byteBuffer.clear();
        byteBuffer.putInt(i);
        byteBuffer.flip();
        channel.write(byteBuffer);
      }
    });

    Future<?> readFuture = executorService.submit(() -> {
      ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES * ints);
      for (int i = 0; i < ints; i++) {
        byteBuffer.clear();
        channel.read(byteBuffer, 0);
        byteBuffer.flip();
        for (int j = 0; byteBuffer.hasRemaining(); j++) {
          assertThat(byteBuffer.getInt()).isEqualTo(j);
        }
      }
    });

    writeFuture.get();
    readFuture.get();
    channel.close();
  }

  @Test
  void testInvalidInitialize() {
    assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, Level.WARNING, () -> {
      try (ConcurrentAsyncFileChannel channel = new ConcurrentAsyncFileChannel(() -> {
        throw new Exception(ConcurrentAsyncFileChannel.class.getSimpleName());
      })) {
        channel.write(ByteBuffer.allocate(1));
        channel.read(ByteBuffer.allocate(1), 1);
      }
    }, logRecord -> {
      assertThat(logRecord.getMessage()).contains(ConcurrentAsyncFileChannel.class.getSimpleName());
      assertThat(logRecord.getThrown().getClass().getSimpleName()).isEqualTo(Exception.class.getSimpleName());
    }));
  }

  @Test
  void testNullInitialize() {
    ByteBuffer buffer = ByteBuffer.allocate(1);
    try (ConcurrentAsyncFileChannel channel = new ConcurrentAsyncFileChannel(Optional::empty)) {
      channel.write(buffer);
      buffer.clear();
      channel.read(buffer, 1);
    }
    assertThat(buffer.position()).isZero();
  }
}