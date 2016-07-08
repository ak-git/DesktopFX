package com.ak.hardware.rsce.comm.interceptor;

import java.nio.ByteBuffer;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class RsceCommandFrameTest {
  @Test
  public void testSimpleCatchRequest() {
    byte[][] expected = {
        {0x01, 0x03, 0x00, 0x20, (byte) 0xF0},
        {0x01, 0x03, 0x01, (byte) 0xE1, 0x30},
        {0x01, 0x03, 0x02, (byte) 0xA1, 0x31},
        {0x01, 0x03, 0x03, 0x60, (byte) 0xF1},
        {0x01, 0x03, 0x04, 0x21, 0x33}
    };
    testSimpleRequest(expected, RsceCommandFrame.Control.CATCH);
  }

  @Test
  public void testSimpleFingerRequest() {
    byte[][] expected = {
        {0x02, 0x03, 0x00, (byte) 0xD0, (byte) 0xF0},
        {0x02, 0x03, 0x01, 0x11, 0x30},
        {0x02, 0x03, 0x02, 0x51, 0x31},
        {0x02, 0x03, 0x03, (byte) 0x90, (byte) 0xF1},
        {0x02, 0x03, 0x04, (byte) 0xD1, 0x33}
    };
    testSimpleRequest(expected, RsceCommandFrame.Control.FINGER);
  }

  @Test
  public void testSimpleRotateRequest() {
    byte[][] expected = {
        {0x03, 0x03, 0x00, (byte) 0x81, 0x30},
        {0x03, 0x03, 0x01, 0x40, (byte) 0xF0},
        {0x03, 0x03, 0x02, 0x00, (byte) 0xF1},
        {0x03, 0x03, 0x03, (byte) 0xC1, 0x31},
        {0x03, 0x03, 0x04, (byte) 0x80, (byte) 0xF3}
    };
    testSimpleRequest(expected, RsceCommandFrame.Control.ROTATE);
  }

  private static void testSimpleRequest(byte[][] expected, RsceCommandFrame.Control servomotorControl) {
    RsceCommandFrame.RequestType[] values = RsceCommandFrame.RequestType.values();
    for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
      RsceCommandFrame.RequestType requestType = values[i];
      RsceCommandFrame request = RsceCommandFrame.simple(servomotorControl, requestType);
      ByteBuffer byteBuffer = ByteBuffer.allocate(expected.length);
      request.writeTo(byteBuffer);
      Assert.assertEquals(expected[i], byteBuffer.array(), String.format("Test [%d], request %s", i, request));
    }
  }

  @Test
  public void testOffRequest() {
    byte[][] expected = {
        {0x00, 0x03, 0x20, 0x70, (byte) 0xe8},
        {0x01, 0x03, 0x20, 0x21, 0x28},
        {0x02, 0x03, 0x20, (byte) 0xD1, 0x28},
        {0x03, 0x03, 0x20, (byte) 0x80, (byte) 0xE8}
    };

    RsceCommandFrame.Control[] values = RsceCommandFrame.Control.values();
    for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
      RsceCommandFrame.Control servomotorControl = values[i];
      RsceCommandFrame request = RsceCommandFrame.off(servomotorControl);
      ByteBuffer byteBuffer = ByteBuffer.allocate(expected[i].length);
      request.writeTo(byteBuffer);
      Assert.assertEquals(expected[i], byteBuffer.array(), String.format("Test [%d], request %s", i, request));
    }
  }

  @Test
  public void testPreciseRequest() {
    byte[][] expected = {
        {0x01, 0x05, 0x0C, 0x00, 0x00, (byte) 0xD9, 0x0F},
        {0x01, 0x05, 0x0C, 0x20, 0x4E, 0x40, (byte) 0xFB},
        {0x01, 0x05, 0x0C, (byte) 0xE0, (byte) 0xB1, 0x50, (byte) 0xBB},
        {0x01, 0x05, 0x0C, (byte) 0xA0, 0x0F, (byte) 0xE1, 0x0B}
    };

    short[] speeds = {0, 20000, -20000, 4000};

    for (int i = 0; i < speeds.length; i++) {
      RsceCommandFrame request = RsceCommandFrame.precise(RsceCommandFrame.Control.CATCH,
          RsceCommandFrame.RequestType.STATUS_I_SPEED_ANGLE, speeds[i]);
      ByteBuffer byteBuffer = ByteBuffer.allocate(expected[i].length);
      request.writeTo(byteBuffer);
      Assert.assertEquals(expected[i], byteBuffer.array(), String.format("Test [%d], request %s", i, request));
    }
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public void testClone() throws CloneNotSupportedException {
    RsceCommandFrame.precise(RsceCommandFrame.Control.ALL, RsceCommandFrame.RequestType.EMPTY).clone();
  }
}