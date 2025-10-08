package com.ak.comm;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import static com.ak.util.UIConstants.UI_DELAY_3SEC;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

class CycleSerialServiceTest {
  @Test
  void testBytesInterceptor() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);

    CycleSerialService<BufferFrame, BufferFrame, ADCVariable> service =
        new CycleSerialService<>(new RampBytesInterceptor(getClass().getName(), BytesInterceptor.BaudRate.BR_115200, 2),
            new ToIntegerConverter<>(ADCVariable.class, 1000));
    service.subscribe(new Flow.Subscriber<>() {
      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        Assertions.assertThat(subscription).isNotNull();
      }

      @Override
      public void onNext(int[] item) {
        fail();
      }

      @Override
      public void onError(Throwable throwable) {
        fail(throwable.getMessage(), throwable);
      }

      @Override
      public void onComplete() {
        fail();
      }
    });
    service.write(new BufferFrame(new byte[] {1, 2}, ByteOrder.nativeOrder()));
    service.refresh(false);
    assertFalse(latch.await(UI_DELAY_3SEC.getSeconds(), TimeUnit.SECONDS));
    service.close();
  }
}