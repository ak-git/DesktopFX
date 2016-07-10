package com.ak.hardware.nmis.comm.serial;

import com.ak.comm.interceptor.DefaultBytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import com.ak.hardware.nmis.comm.interceptor.NmisBytesInterceptor;
import com.ak.hardware.nmis.comm.interceptor.NmisRequest;
import com.ak.hardware.nmis.comm.interceptor.NmisResponseFrame;
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
  public void testBytesInterceptor() {
    CycleSerialService<NmisResponseFrame, NmisRequest> service = new CycleSerialService<>(new NmisBytesInterceptor());
    TestSubscriber<NmisResponseFrame> subscriber = TestSubscriber.create();
    service.getBufferObservable().subscribe(subscriber);
    service.write(NmisRequest.Single.Z_360.buildForAll(NmisRequest.MyoType.OFF, NmisRequest.MyoFrequency.OFF));
    service.write(NmisRequest.Sequence.CATCH_100.build());

    subscriber.assertNotCompleted();
    service.close();
    subscriber.assertCompleted();
    subscriber.assertNoValues();
    subscriber.assertNoErrors();
  }
}