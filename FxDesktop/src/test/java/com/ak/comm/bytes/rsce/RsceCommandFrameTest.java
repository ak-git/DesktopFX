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

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "positionRequests")
  public static void testPositionRequest(@Nonnull byte[] expected, byte position) {
    checkRequest(expected, RsceCommandFrame.position(RsceCommandFrame.Control.CATCH, position));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "rheo12-catch-rotate")
  public static void testInfoRequest(@Nonnull byte[] bytes, @Nonnull int[] rDozenMilliOhms, @Nonnull int[] infoOnes) {
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build();
    Assert.assertNotNull(frame);
    Assert.assertFalse(frame.getRDozenMilliOhms().count() != 0 && rDozenMilliOhms.length == 0);
    if (rDozenMilliOhms.length != 0) {
      Assert.assertEquals(frame.getRDozenMilliOhms().toArray(), rDozenMilliOhms,
          String.format("%s, Ohms = %s", frame, Arrays.toString(frame.getRDozenMilliOhms().toArray())));
    }
    if (infoOnes.length != 0) {
      Assert.assertEquals(frame.getInfoOnes().toArray(), infoOnes,
          String.format("%s, Info = %s", frame, Arrays.toString(frame.getInfoOnes().toArray())));
    }
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