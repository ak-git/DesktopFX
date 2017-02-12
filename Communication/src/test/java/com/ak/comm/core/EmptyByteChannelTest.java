package com.ak.comm.core;

import java.nio.ByteBuffer;

import org.testng.Assert;
import org.testng.annotations.Test;

public class EmptyByteChannelTest {
  private final EmptyByteChannel channel = new EmptyByteChannel();
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
  public void testGetPosition() {
    Assert.assertEquals(channel.position(), 0L);
  }

  @Test
  public void testSetPosition() {
    Assert.assertEquals(channel.position(0), channel);
    Assert.assertEquals(channel.position(1), channel);
  }

  @Test
  public void testSize() {
    Assert.assertEquals(channel.size(), 0);
  }

  @Test
  public void testTruncate() {
    Assert.assertEquals(channel.truncate(0L), channel);
    Assert.assertEquals(channel.truncate(1L), channel);
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