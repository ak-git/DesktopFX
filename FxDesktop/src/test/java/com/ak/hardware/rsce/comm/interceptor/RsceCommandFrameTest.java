package com.ak.hardware.rsce.comm.interceptor;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.Control.ALL;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.Control.CATCH;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.EMPTY;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.STATUS_I_SPEED_ANGLE;

public final class RsceCommandFrameTest {
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
    checkRequest(expected, RsceCommandFrame.precise(CATCH, STATUS_I_SPEED_ANGLE, speed));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "positionRequests")
  public void testPositionRequest(@Nonnull byte[] expected, byte position) {
    checkRequest(expected, RsceCommandFrame.position(CATCH, position));
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

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public void testClone() throws CloneNotSupportedException {
    RsceCommandFrame.precise(ALL, EMPTY).clone();
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