package com.ak.hardware.simple.comm.serial;

import com.ak.comm.serial.CycleSerialService;
import com.ak.hardware.simple.comm.interceptor.DefaultBytesInterceptor;
import org.testng.Assert;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

public final class CycleSerialServiceTest {
  @Test
  public void testDefaultBytesInterceptor() {
    DefaultBytesInterceptor interceptor = new DefaultBytesInterceptor();
    CycleSerialService<Integer, Byte> service = new CycleSerialService<>(interceptor);
    Assert.assertTrue(interceptor.isOpen());
    Assert.assertNotNull(interceptor.name());
    Assert.assertNotNull(interceptor.getPingRequest());
    TestSubscriber<Integer> subscriber = TestSubscriber.create();
    service.getBufferObservable().subscribe(subscriber);
    service.write((byte) 1);
    if (service.isOpen()) {
      service.write((byte) 2);
    }

    subscriber.assertNotCompleted();
    service.close();
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
  }
}