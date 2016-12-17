package com.ak.comm.core;

import java.nio.ByteBuffer;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class SafeByteChannelTest {
  private final SafeByteChannel channel = new SafeByteChannel(getClass().getSimpleName());

  @Test
  public void testWrite() throws Exception {
    Assert.assertFalse(channel.isOpen());
    channel.close();
    ByteBuffer byteBuffer = ByteBuffer.allocate(1);
    byteBuffer.put((byte) 1);
    byteBuffer.flip();

    for (int i = 0; i < 10; i++) {
      Assert.assertEquals(channel.write(byteBuffer), 1);
      byteBuffer.rewind();
    }
    Assert.assertTrue(channel.isOpen());
    channel.close();
    Assert.assertFalse(channel.isOpen());
  }
}