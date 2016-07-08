package com.ak.hardware.tnmi.comm.serial;

import com.ak.comm.interceptor.DefaultBytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import com.ak.hardware.tnmi.comm.interceptor.TnmiBytesInterceptor;
import com.ak.hardware.tnmi.comm.interceptor.TnmiRequest;
import com.ak.hardware.tnmi.comm.interceptor.TnmiResponseFrame;
import org.testng.Assert;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

public final class CycleSerialServiceTest {
  @Test
  public void testDefaultBytesInterceptor() {
    DefaultBytesInterceptor interceptor = new DefaultBytesInterceptor();
    CycleSerialService<Integer, Byte> service = new CycleSerialService<>(interceptor);
    Assert.assertTrue(interceptor.isOpen());
    TestSubscriber<Integer> subscriber = TestSubscriber.create();
    service.getBufferObservable().subscribe(subscriber);
    service.write((byte) 1);

    subscriber.assertNotCompleted();
    service.close();
    subscriber.assertCompleted();
    subscriber.assertNoValues();
    subscriber.assertNoErrors();
  }

  @Test
  public void testTnmiBytesInterceptor() {
    CycleSerialService<TnmiResponseFrame, TnmiRequest> service = new CycleSerialService<>(new TnmiBytesInterceptor());
    TestSubscriber<TnmiResponseFrame> subscriber = TestSubscriber.create();
    service.getBufferObservable().subscribe(subscriber);
    service.write(TnmiRequest.Single.Z_360.buildForAll(TnmiRequest.MyoType.OFF, TnmiRequest.MyoFrequency.OFF));
    service.write(TnmiRequest.Sequence.CATCH_100.build());

    subscriber.assertNotCompleted();
    service.close();
    subscriber.assertCompleted();
    subscriber.assertNoValues();
    subscriber.assertNoErrors();
  }
}