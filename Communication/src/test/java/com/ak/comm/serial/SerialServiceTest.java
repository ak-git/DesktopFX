package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

import com.fazecast.jSerialComm.SerialPort;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SerialServiceTest implements Flow.Subscriber<ByteBuffer> {
  private SerialServiceTest() {
  }

  @Test
  public void test() {
    List<SerialService> services = Arrays.stream(SerialPort.getCommPorts()).map(port -> {
      SerialService serialService = new SerialService(115200);
      serialService.subscribe(this);
      Assert.assertEquals(serialService.write(ByteBuffer.allocate(0)), 0);
      return serialService;
    }).collect(Collectors.toList());

    SerialService singleService = new SerialService(115200);
    singleService.subscribe(this);
    singleService.close();
    Assert.assertTrue(singleService.toString().contains("serialPort"));
    Assert.assertFalse(singleService.isOpen());
    services.forEach(SerialService::close);
  }

  @Override
  public void onSubscribe(Flow.Subscription s) {
  }

  @Override
  public void onNext(ByteBuffer buffer) {
  }

  @Override
  public void onError(Throwable e) {
    Assert.assertNotNull(e);
    Assert.fail();
  }

  @Override
  public void onComplete() {
  }
}