package com.ak.hardware.rsce.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.Control.ALL;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.Control.CATCH;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.Control.FINGER;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.Control.ROTATE;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.EMPTY;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.STATUS_I;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.STATUS_I_ANGLE;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.STATUS_I_SPEED;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.STATUS_I_SPEED_ANGLE;

public final class RsceCommandFrameTest {
  @DataProvider(name = "simpleRequests", parallel = true)
  public static Object[][] simpleSimpleRequests() {
    return new Object[][] {
        {new byte[] {0x01, 0x03, 0x00, 0x20, (byte) 0xF0}, CATCH, EMPTY},
        {new byte[] {0x01, 0x03, 0x01, (byte) 0xE1, 0x30}, CATCH, STATUS_I},
        {new byte[] {0x01, 0x03, 0x02, (byte) 0xA1, 0x31}, CATCH, STATUS_I_SPEED},
        {new byte[] {0x01, 0x03, 0x03, 0x60, (byte) 0xF1}, CATCH, STATUS_I_ANGLE},
        {new byte[] {0x01, 0x03, 0x04, 0x21, 0x33}, CATCH, STATUS_I_SPEED_ANGLE},

        {new byte[] {0x02, 0x03, 0x00, (byte) 0xD0, (byte) 0xF0}, FINGER, EMPTY},
        {new byte[] {0x02, 0x03, 0x01, 0x11, 0x30}, FINGER, STATUS_I},
        {new byte[] {0x02, 0x03, 0x02, 0x51, 0x31}, FINGER, STATUS_I_SPEED},
        {new byte[] {0x02, 0x03, 0x03, (byte) 0x90, (byte) 0xF1}, FINGER, STATUS_I_ANGLE},
        {new byte[] {0x02, 0x03, 0x04, (byte) 0xD1, 0x33}, FINGER, STATUS_I_SPEED_ANGLE},

        {new byte[] {0x03, 0x03, 0x00, (byte) 0x81, 0x30}, ROTATE, EMPTY},
        {new byte[] {0x03, 0x03, 0x01, 0x40, (byte) 0xF0}, ROTATE, STATUS_I},
        {new byte[] {0x03, 0x03, 0x02, 0x00, (byte) 0xF1}, ROTATE, STATUS_I_SPEED},
        {new byte[] {0x03, 0x03, 0x03, (byte) 0xC1, 0x31}, ROTATE, STATUS_I_ANGLE},
        {new byte[] {0x03, 0x03, 0x04, (byte) 0x80, (byte) 0xF3}, ROTATE, STATUS_I_SPEED_ANGLE},
    };
  }

  @Test(dataProvider = "simpleRequests")
  public void testSimpleRequest(@Nonnull byte[] expected, @Nonnull RsceCommandFrame.Control control, @Nonnull RsceCommandFrame.RequestType type) {
    checkRequest(expected, RsceCommandFrame.simple(control, type));
  }

  @DataProvider(name = "offRequests", parallel = true)
  public static Object[][] offRequests() {
    byte[][] expected = {
        {0x00, 0x03, 0x20, 0x70, (byte) 0xe8},
        {0x01, 0x03, 0x20, 0x21, 0x28},
        {0x02, 0x03, 0x20, (byte) 0xD1, 0x28},
        {0x03, 0x03, 0x20, (byte) 0x80, (byte) 0xE8}
    };

    Object[][] values = new Object[expected.length][2];
    for (int i = 0; i < values.length; i++) {
      values[i][0] = expected[i];
      values[i][1] = RsceCommandFrame.Control.values()[i];
    }
    return values;
  }

  @Test(dataProvider = "offRequests")
  public void testOffRequest(@Nonnull byte[] expected, @Nonnull RsceCommandFrame.Control control) {
    checkRequest(expected, RsceCommandFrame.off(control));
  }

  @DataProvider(name = "preciseRequests", parallel = true)
  public static Object[][] preciseRequests() {
    byte[][] expected = {
        {0x01, 0x05, 0x0C, 0x00, 0x00, (byte) 0xD9, 0x0F},
        {0x01, 0x05, 0x0C, 0x20, 0x4E, 0x40, (byte) 0xFB},
        {0x01, 0x05, 0x0C, (byte) 0xE0, (byte) 0xB1, 0x50, (byte) 0xBB},
        {0x01, 0x05, 0x0C, (byte) 0xA0, 0x0F, (byte) 0xE1, 0x0B}
    };

    short[] speeds = {0, 20000, -20000, 4000};

    Object[][] values = new Object[expected.length][2];
    for (int i = 0; i < values.length; i++) {
      values[i][0] = expected[i];
      values[i][1] = speeds[i];
    }
    return values;
  }

  @Test(dataProvider = "preciseRequests")
  public void testPreciseRequest(@Nonnull byte[] expected, short speed) {
    checkRequest(expected,
        RsceCommandFrame.precise(CATCH, STATUS_I_SPEED_ANGLE, speed));
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public void testClone() throws CloneNotSupportedException {
    RsceCommandFrame.precise(ALL, EMPTY).clone();
  }

  private static void checkRequest(@Nonnull byte[] expected, @Nonnull RsceCommandFrame request) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(expected.length);
    request.writeTo(byteBuffer);
    Assert.assertEquals(byteBuffer.array(), expected, request.toString());
  }
}