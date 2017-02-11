package com.ak.comm;

import java.nio.ByteOrder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.util.UIConstants.UI_DELAY;

public class CycleSerialServiceTest {
  private enum SingleVariable implements Variable {
    SINGLE_VARIABLE
  }

  private CycleSerialServiceTest() {
  }

  @Test
  public static void testBytesInterceptor() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);

    CycleSerialService<BufferFrame, BufferFrame, SingleVariable> service =
        new CycleSerialService<>(new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 2),
            new ToIntegerConverter<>(SingleVariable.class));
    TestSubscriber<int[]> subscriber = TestSubscriber.create();
    service.subscribe(subscriber);
    service.write(new BufferFrame(new byte[] {1, 2}, ByteOrder.nativeOrder()));
    Assert.assertFalse(latch.await(UI_DELAY.getSeconds(), TimeUnit.SECONDS));
    subscriber.assertNotComplete();
    service.close();
    subscriber.assertNotComplete();
    subscriber.assertNoErrors();
  }
}