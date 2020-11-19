package com.ak.comm.bytes.rsce;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RsceCommandFrameTest {
  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "simpleRequests")
  public void testSimpleRequest(@Nonnull byte[] expected, @Nonnull RsceCommandFrame.Control control, @Nonnull RsceCommandFrame.RequestType type) {
    checkRequest(expected, RsceCommandFrame.simple(control, type));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "offRequests")
  public void testOffRequest(@Nonnull byte[] expected, @Nonnull RsceCommandFrame.Control control) {
    checkRequest(expected, RsceCommandFrame.off(control));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "preciseRequests")
  public void testPreciseRequest(@Nonnull byte[] expected, short speed) {
    checkRequest(expected, RsceCommandFrame.precise(RsceCommandFrame.Control.CATCH, RsceCommandFrame.RequestType.STATUS_I_SPEED_ANGLE, speed));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "positionCatchRequests")
  public void testPositionCatchRequest(@Nonnull byte[] expected, byte position) {
    checkRequest(expected, RsceCommandFrame.position(RsceCommandFrame.Control.CATCH, position));
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(expected)).build();
    Assert.assertNotNull(frame);
    Assert.assertEquals(frame.extract(RsceCommandFrame.FrameField.OPEN_PERCENT, -1), position,
        "%s, Position = %d".formatted(String.valueOf(frame), frame.extract(RsceCommandFrame.FrameField.OPEN_PERCENT, -1)));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "positionRotateRequests")
  public void testPositionRotateRequest(@Nonnull byte[] expected, byte position) {
    checkRequest(expected, RsceCommandFrame.position(RsceCommandFrame.Control.ROTATE, position));
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(expected)).build();
    Assert.assertNotNull(frame);
    Assert.assertEquals(frame.extract(RsceCommandFrame.FrameField.ROTATE_PERCENT, -1), position,
        "%s, Position = %d".formatted(String.valueOf(frame), frame.extract(RsceCommandFrame.FrameField.ROTATE_PERCENT, -1)));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "rheo12-info")
  public void testInfoRequest(@Nonnull byte[] bytes, @Nonnull int[] rDozenMilliOhms, @Nonnull int[] infoOnes) {
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build();
    Assert.assertNotNull(frame);
    Assert.assertEquals(frame.extract(RsceCommandFrame.FrameField.R1_DOZEN_MILLI_OHM, 0), rDozenMilliOhms[0],
        "%s, Ohms = %d".formatted(String.valueOf(frame), frame.extract(RsceCommandFrame.FrameField.R1_DOZEN_MILLI_OHM, 0)));
    Assert.assertEquals(frame.extract(RsceCommandFrame.FrameField.R2_DOZEN_MILLI_OHM, 0), rDozenMilliOhms[1],
        "%s, Ohms = %d".formatted(String.valueOf(frame), frame.extract(RsceCommandFrame.FrameField.R2_DOZEN_MILLI_OHM, 0)));
    Assert.assertEquals(frame.extract(RsceCommandFrame.FrameField.ACCELEROMETER, 0), infoOnes[0],
        "%s, Info = %d".formatted(String.valueOf(frame), frame.extract(RsceCommandFrame.FrameField.ACCELEROMETER, 0)));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "finger")
  public void testInfoRequest(@Nonnull byte[] bytes, int fingerSpeed) {
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build();
    Assert.assertNotNull(frame);
    Assert.assertEquals(frame.extract(RsceCommandFrame.FrameField.FINGER, 0), fingerSpeed,
        "%s, finger = %d".formatted(String.valueOf(frame), frame.extract(RsceCommandFrame.FrameField.FINGER, 0)));
  }

  @Test
  public void testInvalidInfoRequest() {
    RsceCommandFrame frame = new RsceCommandFrame.RequestBuilder(RsceCommandFrame.Control.ALL, RsceCommandFrame.ActionType.NONE, RsceCommandFrame.RequestType.EMPTY).build();
    Assert.assertNotNull(frame);
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "invalidRequests")
  public void testInvalidRequests(@Nonnull byte[] bytes) {
    Assert.assertNull(new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build(), Arrays.toString(bytes));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "invalidRequests",
      expectedExceptions = UnsupportedOperationException.class)
  public void testInvalidMethod(@Nonnull byte[] bytes) {
    new RsceCommandFrame.ResponseBuilder().bufferLimit(ByteBuffer.wrap(bytes));
  }

  private static void checkRequest(@Nonnull byte[] expected, @Nonnull RsceCommandFrame request) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(expected.length);
    request.writeTo(byteBuffer);
    Assert.assertEquals(byteBuffer.array(), expected, request.toString());

    RsceCommandFrame that = new RsceCommandFrame.ResponseBuilder(byteBuffer).build();
    Assert.assertNotNull(that);
    Assert.assertEquals(that, request);
    Assert.assertEquals(that, that);
    Assert.assertEquals(that.hashCode(), request.hashCode());
    Assert.assertNotEquals(that, byteBuffer);
    Assert.assertNotEquals(byteBuffer, that);
  }
}