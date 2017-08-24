package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.comm.logging.LogBuilders;
import com.ak.comm.util.LogUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConcurrentAsyncFileChannelTest {
  private static final Logger LOGGER = Logger.getLogger(ConcurrentAsyncFileChannel.class.getName());

  private ConcurrentAsyncFileChannelTest() {
  }

  @Test
  public static void testWriteAndRead() {
    ConcurrentAsyncFileChannel channel = new ConcurrentAsyncFileChannel(() -> {
      Path path = LogBuilders.TIME.build(ConcurrentAsyncFileChannelTest.class.getSimpleName()).getPath();
      return AsynchronousFileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.READ);
    });

    channel.close();
    ByteBuffer byteBuffer = ByteBuffer.allocate(1);
    byteBuffer.put((byte) 1);
    byteBuffer.flip();

    for (int i = 0; i < 10; i++) {
      Assert.assertEquals(channel.write(byteBuffer), 1);
      byteBuffer.clear();
      Assert.assertEquals(channel.read(byteBuffer, 0), 1);
      byteBuffer.rewind();
    }
    channel.close();
  }

  @Test
  public static void testInvalidInitialize() {
    Assert.assertTrue(LogUtils.isSubstituteLogLevel(LOGGER, Level.WARNING, () -> {
      ConcurrentAsyncFileChannel channel = new ConcurrentAsyncFileChannel(() -> {
        throw new Exception(ConcurrentAsyncFileChannel.class.getSimpleName());
      });
      Assert.assertEquals(channel.write(ByteBuffer.allocate(1)), -1);
      Assert.assertEquals(channel.read(ByteBuffer.allocate(1), 1), -1);
    }, logRecord -> {
      Assert.assertTrue(logRecord.getMessage().contains(ConcurrentAsyncFileChannel.class.getSimpleName()));
      Assert.assertEquals(logRecord.getThrown().getClass().getSimpleName(), Exception.class.getSimpleName());
    }));
  }
}