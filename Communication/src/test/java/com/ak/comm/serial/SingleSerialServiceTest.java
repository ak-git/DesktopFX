package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import jssc.SerialPortException;
import jssc.SerialPortList;
import org.testng.Assert;
import org.testng.annotations.Test;
import rx.Observer;

public final class SingleSerialServiceTest implements Observer<ByteBuffer> {
  @Test
  public void test() throws InterruptedException {
    String[] portNames = SerialPortList.getPortNames();
    Collection<SingleSerialService> services = new ArrayList<>(portNames.length);
    for (String port : portNames) {
      SingleSerialService singleSerialService = new SingleSerialService(115200);
      Assert.assertEquals(singleSerialService.getPortName(), port);
      singleSerialService.getByteBuffer().subscribe(this);
      Assert.assertTrue(singleSerialService.isWrite(
          new byte[] {0x7E, (byte) 0x81, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07}));
      services.add(singleSerialService);
    }

    SingleSerialService singleSerialService = new SingleSerialService(115200);
    singleSerialService.getByteBuffer().subscribe(this);
    services.add(singleSerialService);
    TimeUnit.SECONDS.sleep(1);
    services.forEach(SingleSerialService::close);
  }

  @Override
  public void onCompleted() {
  }

  @Override
  public void onError(Throwable e) {
    SerialPortException cast = SerialPortException.class.cast(e);
    Assert.assertNotNull(cast);
    Assert.assertEquals(cast.getMethodName(), "openPort()");
  }

  @Override
  public void onNext(ByteBuffer buffer) {
  }
}