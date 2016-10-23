package com.ak.hardware.nmis.comm.serial;

import com.ak.comm.serial.CycleSerialService;
import com.ak.hardware.nmis.comm.interceptor.NmisBytesInterceptor;
import com.ak.hardware.nmis.comm.interceptor.NmisRequest;
import com.ak.hardware.nmis.comm.interceptor.NmisResponseFrame;
import org.testng.annotations.Test;
import rx.observers.TestSubscriber;

public final class NmisCycleSerialServiceTest {
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
    subscriber.assertNoErrors();
  }
}