package com.ak.comm;

import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.interceptor.nmis.NmisBytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.annotations.Test;

public final class CycleSerialServiceTest {
  @Test
  public void testBytesInterceptor() {
    CycleSerialService<NmisResponseFrame, NmisRequest> service = new CycleSerialService<>(new NmisBytesInterceptor());
    TestSubscriber<NmisResponseFrame> subscriber = TestSubscriber.create();
    service.subscribe(subscriber);
    service.write(NmisRequest.Single.Z_360.buildForAll(NmisRequest.MyoType.OFF, NmisRequest.MyoFrequency.OFF));
    service.write(NmisRequest.Sequence.CATCH_100.build());

    subscriber.assertNotComplete();
    service.cancel();
    subscriber.assertNotComplete();
    subscriber.assertNoErrors();
  }
}