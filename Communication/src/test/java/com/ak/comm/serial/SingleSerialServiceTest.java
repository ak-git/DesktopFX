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
    List<SingleSerialService> services = Stream.of(SerialPortList.getPortNames()).map(port -> {
      SingleSerialService singleSerialService = new SingleSerialService(115200);
      Assert.assertEquals(singleSerialService.getPortName(), port);
      singleSerialService.getByteBuffer().subscribe(this);
      Assert.assertTrue(singleSerialService.isWrite(new byte[] {0x7E}));
      return singleSerialService;
    }).collect(Collectors.toList());

    SingleSerialService singleSerialService = new SingleSerialService(115200);
    singleSerialService.getByteBuffer().subscribe(this);
    singleSerialService.close();
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