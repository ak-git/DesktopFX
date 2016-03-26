package com.ak.comm.serial;

import com.ak.comm.interceptor.DefaultBytesInterceptor;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

public final class CycleSerialServiceTest {
  @Test
  public void testDefaultBytesInterceptor() {
    CycleSerialService<Integer, Byte> service = new CycleSerialService<>(115200, new DefaultBytesInterceptor());
    TestSubscriber<Integer> subscriber = TestSubscriber.create();
    service.getBufferObservable().subscribe(subscriber);
    service.write((byte) 0);
    subscriber.assertNotCompleted();
    service.close();
    subscriber.assertCompleted();
    subscriber.assertNoValues();
    subscriber.assertNoErrors();
  }
}