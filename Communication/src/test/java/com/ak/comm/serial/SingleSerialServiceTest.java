package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jssc.SerialPortException;
import jssc.SerialPortList;
import org.testng.Assert;
import org.testng.annotations.Test;
import rx.Observer;

public final class SingleSerialServiceTest implements Observer<ByteBuffer> {
  @Test
  public void test() {
    List<SerialService> services = Stream.of(SerialPortList.getPortNames()).map(port -> {
      SerialService serialService = new SingleSerialService(115200);
      Assert.assertEquals(serialService.getPortName(), port);
      serialService.getBufferObservable().subscribe(this);
      Assert.assertTrue(serialService.isWrite(new byte[] {0x7E}));
      return serialService;
    }).collect(Collectors.toList());

    SerialService singleSerialService = new SingleSerialService(115200);
    singleSerialService.getBufferObservable().subscribe(this);
    singleSerialService.close();
    services.forEach(SerialService::close);
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