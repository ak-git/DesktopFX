package com.ak.hardware.simple.comm.serial;

import com.ak.comm.interceptor.simple.DefaultBytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class CycleSerialServiceTest {
  @Test
  public void testDefaultBytesInterceptor() {
    DefaultBytesInterceptor interceptor = new DefaultBytesInterceptor();
    CycleSerialService<Integer, Byte> service = new CycleSerialService<>(interceptor);
    Assert.assertNotNull(interceptor.name());
    Assert.assertNotNull(interceptor.getPingRequest());
    TestSubscriber<Integer> subscriber = TestSubscriber.create();
    service.subscribe(subscriber);
    service.write((byte) 1);
    service.write((byte) 2);
    subscriber.assertNotComplete();
    service.cancel();
    subscriber.assertNotComplete();
    subscriber.assertNoErrors();
  }
}