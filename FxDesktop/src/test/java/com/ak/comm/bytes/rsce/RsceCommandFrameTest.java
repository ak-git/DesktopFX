package com.ak.comm.bytes.rsce;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RsceCommandFrameTest {
  private RsceCommandFrameTest() {
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "simpleRequests")
  public static void testSimpleRequest(@Nonnull byte[] expected, @Nonnull RsceCommandFrame.Control control, @Nonnull RsceCommandFrame.RequestType type) {
    checkRequest(expected, RsceCommandFrame.simple(control, type));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "offRequests")
  public static void testOffRequest(@Nonnull byte[] expected, @Nonnull RsceCommandFrame.Control control) {
    checkRequest(expected, RsceCommandFrame.off(control));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "preciseRequests")
  public static void testPreciseRequest(@Nonnull byte[] expected, short speed) {
    checkRequest(expected, RsceCommandFrame.precise(RsceCommandFrame.Control.CATCH, RsceCommandFrame.RequestType.STATUS_I_SPEED_ANGLE, speed));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "positionCatchRequests")
  public static void testPositionCatchRequest(@Nonnull byte[] expected, byte position) {
    checkRequest(expected, RsceCommandFrame.position(RsceCommandFrame.Control.CATCH, position));
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(expected)).build();
    Assert.assertNotNull(frame);
    Assert.assertEquals(frame.extract(RsceCommandFrame.FrameField.OPEN_PERCENT, -1), position,
        String.format("%s, Position = %s", frame, frame.extract(RsceCommandFrame.FrameField.OPEN_PERCENT, -1)));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "positionRotateRequests")
  public static void testPositionRotateRequest(@Nonnull byte[] expected, byte position) {
    checkRequest(expected, RsceCommandFrame.position(RsceCommandFrame.Control.ROTATE, position));
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(expected)).build();
    Assert.assertNotNull(frame);
    Assert.assertEquals(frame.extract(RsceCommandFrame.FrameField.ROTATE_PERCENT, -1), position,
        String.format("%s, Position = %s", frame, frame.extract(RsceCommandFrame.FrameField.ROTATE_PERCENT, -1)));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "rheo12-info")
  public static void testInfoRequest(@Nonnull byte[] bytes, @Nonnull int[] rDozenMilliOhms, @Nonnull int[] infoOnes) {
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build();
    Assert.assertNotNull(frame);
    Assert.assertEquals(frame.extract(RsceCommandFrame.FrameField.R1_DOZEN_MILLI_OHM, 0), rDozenMilliOhms[0],
        String.format("%s, Ohms = %s", frame, frame.extract(RsceCommandFrame.FrameField.R1_DOZEN_MILLI_OHM, 0)));
    Assert.assertEquals(frame.extract(RsceCommandFrame.FrameField.R2_DOZEN_MILLI_OHM, 0), rDozenMilliOhms[1],
        String.format("%s, Ohms = %s", frame, frame.extract(RsceCommandFrame.FrameField.R2_DOZEN_MILLI_OHM, 0)));
    Assert.assertEquals(frame.extract(RsceCommandFrame.FrameField.INFO, 0), infoOnes[0],
        String.format("%s, Info = %s", frame, frame.extract(RsceCommandFrame.FrameField.INFO, 0)));
  }

  @Test
  public static void testInvalidInfoRequest() {
    RsceCommandFrame frame = new RsceCommandFrame.RequestBuilder(RsceCommandFrame.Control.ALL, RsceCommandFrame.ActionType.NONE, RsceCommandFrame.RequestType.EMPTY).build();
    Assert.assertNotNull(frame);
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "invalidRequests")
  public static void testInvalidRequests(@Nonnull byte[] bytes) {
    Assert.assertNull(new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build(), Arrays.toString(bytes));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "invalidRequests",
      expectedExceptions = UnsupportedOperationException.class)
  public static void testInvalidMethod(@Nonnull byte[] bytes) {
    new RsceCommandFrame.ResponseBuilder().bufferLimit(ByteBuffer.wrap(bytes));
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testClone() throws CloneNotSupportedException {
    RsceCommandFrame.precise(RsceCommandFrame.Control.ALL, RsceCommandFrame.RequestType.EMPTY).clone();
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