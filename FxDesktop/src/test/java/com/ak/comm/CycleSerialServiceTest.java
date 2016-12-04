package com.ak.comm;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.converter.rsce.RsceConverter;
import com.ak.comm.interceptor.nmisr.NmisRsceBytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.util.UIConstants.UI_DELAY;

public final class CycleSerialServiceTest {
  @Test
  public void testBytesInterceptor() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);

    CycleSerialService<RsceCommandFrame, NmisRequest> service = new CycleSerialService<>(new NmisRsceBytesInterceptor(), new RsceConverter());
    TestSubscriber<int[]> subscriber = TestSubscriber.create();
    service.subscribe(subscriber);
    service.write(NmisRequest.Single.Z_360.buildForAll(NmisRequest.MyoType.OFF, NmisRequest.MyoFrequency.OFF));
    service.write(NmisRequest.Sequence.CATCH_100.build());

    Assert.assertFalse(latch.await(UI_DELAY.getSeconds(), TimeUnit.SECONDS));
    subscriber.assertNotComplete();
    service.cancel();
    subscriber.assertNotComplete();
    subscriber.assertNoErrors();
  }
}