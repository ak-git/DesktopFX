package com.ak.hardware.rsce.comm.interceptor;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

import com.ak.comm.interceptor.BytesInterceptor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import rx.Subscription;
import rx.observers.TestSubscriber;

import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.ActionType.NONE;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.Control.CATCH;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.EMPTY;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.STATUS_I;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.STATUS_I_ANGLE;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.STATUS_I_SPEED;
import static com.ak.hardware.rsce.comm.interceptor.RsceCommandFrame.RequestType.STATUS_I_SPEED_ANGLE;

public final class RsceBytesInterceptor2Test {
  private final BytesInterceptor<RsceCommandFrame, RsceCommandFrame> interceptor = new RsceBytesInterceptor();
  private final ByteBuffer byteBuffer = ByteBuffer.allocate(RsceCommandFrame.MAX_CAPACITY * 2);

  @DataProvider(name = "data")
  public Object[][] data() {
    return new Object[][] {
        //added 0x00 at start
        {new byte[] {0x00, 0x01, 0x03, 0x00, 0x20, (byte) 0xF0}, RsceCommandFrame.simple(CATCH, EMPTY)},
        //added 0x00 at start
        {new byte[] {0x00, 0x01, 0x03, 0x01, (byte) 0xE1, 0x30}, RsceCommandFrame.simple(CATCH, STATUS_I)},
        {new byte[] {0x01, 0x03, 0x02, (byte) 0xA1, 0x31}, RsceCommandFrame.simple(CATCH, STATUS_I_SPEED)},
        //error in CRC16
        {new byte[] {0x01, 0x03, 0x03, 0x60, (byte) 0xF1 + 1}, RsceCommandFrame.off(CATCH)},
        {new byte[] {0x01, 0x03, 0x04, 0x21, 0x33}, RsceCommandFrame.simple(CATCH, STATUS_I_SPEED_ANGLE)},
        {new byte[] {0x01, 0x08, 0x03, 0x01, 0x09, 0x0F, (byte) 0x9F, (byte) 0xA2, (byte) 0xEE, 0x22},
            new RsceCommandFrame.Builder(CATCH, NONE, STATUS_I_ANGLE).
                addParam((byte) 0x01).addParam((short) 3849).addParam((short) 41631).build()}
    };
  }

  @Test(dataProvider = "data")
  public void testInterceptor(@Nonnull byte[] bytes, @Nonnull RsceCommandFrame request) {
    TestSubscriber<RsceCommandFrame> subscriber = TestSubscriber.create();
    Subscription subscription = interceptor.getBufferObservable().subscribe(subscriber);

    byteBuffer.clear();
    byteBuffer.put(bytes);
    byteBuffer.flip();
    int countResponses = interceptor.write(byteBuffer);
    if (countResponses == 0) {
      subscriber.assertNoValues();
    }
    else {
      Assert.assertEquals(countResponses, 1);
      subscriber.assertValue(request);
    }
    Assert.assertTrue(interceptor.put(request).remaining() > 0);
    subscriber.assertNoErrors();
    subscription.unsubscribe();
  }

  @AfterClass
  public void tearDown() throws Exception {
    interceptor.close();
  }
}