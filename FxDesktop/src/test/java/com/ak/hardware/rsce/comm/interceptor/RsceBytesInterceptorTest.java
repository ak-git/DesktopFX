package com.ak.hardware.rsce.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.Control.CATCH;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.STATUS_I_SPEED_ANGLE;

public final class RsceBytesInterceptorTest {
  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "simpleRequests")
  public void testSimpleRequest(@Nonnull byte[] bytes, @Nonnull RsceCommandFrame.Control control, @Nonnull RsceCommandFrame.RequestType type) {
    checkResponse(bytes, RsceCommandFrame.simple(control, type));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "offRequests")
  public void testOffRequest(@Nonnull byte[] expected, @Nonnull RsceCommandFrame.Control control) {
    checkResponse(expected, RsceCommandFrame.off(control));
  }

  @Test(dataProviderClass = RsceTestDataProvider.class, dataProvider = "preciseRequests")
  public void testPreciseRequest(@Nonnull byte[] expected, short speed) {
    checkResponse(expected, RsceCommandFrame.precise(CATCH, STATUS_I_SPEED_ANGLE, speed));
  }

  private static void checkResponse(@Nonnull byte[] bytes, @Nonnull RsceCommandFrame request) {
    RsceBytesInterceptor interceptor = new RsceBytesInterceptor();
    TestSubscriber<RsceCommandFrame> subscriber = TestSubscriber.create();
    interceptor.getBufferObservable().subscribe(subscriber);

    int countResponses = interceptor.write(ByteBuffer.wrap(bytes));
    if (countResponses == 0) {
      subscriber.assertNoValues();
    }
    else {
      Assert.assertEquals(countResponses, 1);
      subscriber.assertValue(request);
    }
    Assert.assertTrue(interceptor.put(request).remaining() > 0);
    interceptor.close();
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
  }
}