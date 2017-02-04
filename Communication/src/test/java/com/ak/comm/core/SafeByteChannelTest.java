package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.comm.util.LogUtils;
import com.ak.logging.BinaryLogBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class SafeByteChannelTest {
  private static final Logger LOGGER = Logger.getLogger(SafeByteChannel.class.getName());

  private final SafeByteChannel channel = new SafeByteChannel(() -> {
    Path path = new BinaryLogBuilder().fileNameWithTime(getClass().getSimpleName()).build().getPath();
    return Files.newByteChannel(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.READ);
  });

  @Test
  public void testWriteAndRead() {
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
  public void testInvalidInitialize() {
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