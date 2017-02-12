package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jssc.SerialPortException;
import jssc.SerialPortList;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.testng.Assert;
import org.testng.annotations.Test;

import static jssc.SerialPort.BAUDRATE_115200;

public class SerialServiceTest implements Subscriber<ByteBuffer> {
  private SerialServiceTest() {
  }

  @Test
  public void test() {
    List<SerialService> services = Stream.of(SerialPortList.getPortNames()).map(port -> {
      SerialService serialService = new SerialService(BAUDRATE_115200);
      serialService.subscribe(this);
      Assert.assertEquals(serialService.write(ByteBuffer.allocate(0)), 0);
      return serialService;
    }).collect(Collectors.toList());

    SerialService singleService = new SerialService(BAUDRATE_115200);
    singleService.subscribe(this);
    singleService.close();
    Assert.assertTrue(singleService.toString().contains("serialPort"));
    Assert.assertFalse(singleService.isOpen());
    services.forEach(SerialService::close);
  }

  @Override
  public void onSubscribe(Subscription s) {
  }

  @Override
  public void onNext(ByteBuffer buffer) {
  }

  @Override
  public void onError(Throwable e) {
    Assert.assertNotNull(e);
    if (e instanceof SerialPortException) {
      Assert.assertEquals(((SerialPortException) e).getMethodName(), "openPort()");
    }
    else {
      Assert.fail();
    }
  }

  @Override
  public void onComplete() {
  }
}