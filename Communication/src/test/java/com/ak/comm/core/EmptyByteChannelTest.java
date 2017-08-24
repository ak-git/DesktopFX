package com.ak.comm.core;

import java.nio.ByteBuffer;

import org.testng.Assert;
import org.testng.annotations.Test;

public class EmptyByteChannelTest {
  private final EmptyByteChannel channel = EmptyByteChannel.INSTANCE;
  private final ByteBuffer byteBuffer = ByteBuffer.allocate(1);

  private EmptyByteChannelTest() {
  }

  @Test
  public void testRead() {
    Assert.assertEquals(channel.read(byteBuffer), 0);
  }

  @Test
  public void testWrite() {
    Assert.assertEquals(channel.write(byteBuffer), 0);
  }

  @Test
  public void testIsOpen() {
    Assert.assertFalse(channel.isOpen());
  }

  @Test
  public void testClose() {
    channel.close();
  }
}