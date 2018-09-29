package com.ak.comm;

import java.nio.ByteOrder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.util.UIConstants.UI_DELAY;

public class CycleSerialServiceTest {
  private CycleSerialServiceTest() {
  }

  @Test
  public static void testBytesInterceptor() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);

    CycleSerialService<BufferFrame, BufferFrame, ADCVariable> service =
        new CycleSerialService<>(new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 2),
            new ToIntegerConverter<>(ADCVariable.class, 1000));
    service.subscribe(new Flow.Subscriber<>() {
      @Override
      public void onSubscribe(Flow.Subscription subscription) {
      }

      @Override
      public void onNext(int[] item) {
      }

      @Override
      public void onError(Throwable throwable) {
        Assert.fail(throwable.getMessage(), throwable);
      }

      @Override
      public void onComplete() {
        Assert.fail();
      }
    });
    service.write(new BufferFrame(new byte[] {1, 2}, ByteOrder.nativeOrder()));
    service.refresh();
    Assert.assertFalse(latch.await(UI_DELAY.getSeconds(), TimeUnit.SECONDS));
    service.close();
  }
}