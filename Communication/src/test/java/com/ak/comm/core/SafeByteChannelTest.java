package com.ak.comm.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.comm.logging.BinaryLogBuilder;
import com.ak.comm.util.LogUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SafeByteChannelTest {
  private static final Logger LOGGER = Logger.getLogger(SafeByteChannel.class.getName());

  private SafeByteChannelTest() {
  }

  @Test
  public static void testWriteAndRead() {
    SafeByteChannel channel = new SafeByteChannel(() -> {
      Path path = BinaryLogBuilder.TIME.build(SafeByteChannelTest.class.getSimpleName()).getPath();
      return Files.newByteChannel(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.READ);
    });

    Assert.assertFalse(channel.isOpen());
    channel.close();
    ByteBuffer byteBuffer = ByteBuffer.allocate(1);
    byteBuffer.put((byte) 1);
    byteBuffer.flip();

    for (int i = 0; i < 10; i++) {
      Assert.assertEquals(channel.write(byteBuffer), 1);
      byteBuffer.clear();
      Assert.assertEquals(channel.read(byteBuffer), -1);
      byteBuffer.rewind();
    }
    Assert.assertTrue(channel.isOpen());
    channel.close();
    Assert.assertFalse(channel.isOpen());
  }

  @Test
  public static void testInvalidOperations() {
    SafeByteChannel channel = new SafeByteChannel(() -> new SeekableByteChannel() {
      @Override
      public int read(ByteBuffer dst) throws IOException {
        throw new IOException("read");
      }

      @Override
      public int write(ByteBuffer src) throws IOException {
        throw new IOException("write");
      }

      @Override
      public long position() {
        return 0;
      }

      @Override
      public SeekableByteChannel position(long newPosition) {
        return null;
      }

      @Override
      public long size() {
        return 0;
      }

      @Override
      public SeekableByteChannel truncate(long size) {
        return null;
      }

      @Override
      public boolean isOpen() {
        return false;
      }

      @Override
      public void close() throws IOException {
        throw new IOException("close");
      }
    });
    Assert.assertTrue(LogUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
        () -> Assert.assertEquals(channel.write(ByteBuffer.allocate(1)), -1),
        logRecord -> Assert.assertEquals(logRecord.getMessage(), "write")));

    Assert.assertTrue(LogUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
        () -> Assert.assertEquals(channel.read(ByteBuffer.allocate(1)), -1),
        logRecord -> Assert.assertEquals(logRecord.getMessage(), "read")));

    Assert.assertTrue(LogUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS, channel::close,
        logRecord -> Assert.assertEquals(logRecord.getMessage(), "close")));
  }

  @Test
  public static void testInvalidInitialize() {
    Assert.assertTrue(LogUtils.isSubstituteLogLevel(LOGGER, Level.WARNING, () -> {
      SafeByteChannel channel = new SafeByteChannel(() -> {
        throw new Exception();
      });
      Assert.assertFalse(channel.isOpen());
      Assert.assertEquals(channel.write(ByteBuffer.allocate(1)), 0);
      Assert.assertEquals(channel.read(ByteBuffer.allocate(1)), 0);
      Assert.assertFalse(channel.isOpen());
    }, logRecord -> {
      Assert.assertTrue(logRecord.getMessage().contains(SafeByteChannelTest.class.getSimpleName()));
      Assert.assertEquals(logRecord.getThrown().getClass().getSimpleName(), Exception.class.getSimpleName());
    }));
  }
}