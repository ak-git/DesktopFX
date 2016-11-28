package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ak.comm.interceptor.simple.DefaultBytesInterceptor;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class SerialServiceTest implements Subscriber<ByteBuffer> {
  @Test
  public void test() {
    List<SerialService> services = Stream.of(SerialPortList.getPortNames()).map(port -> {
      SerialService serialService = new SerialService(new DefaultBytesInterceptor());
      serialService.subscribe(this);
      Assert.assertEquals(serialService.write(ByteBuffer.allocate(0)), 0);
      return serialService;
    }).collect(Collectors.toList());

    SerialService singleService = new SerialService(new DefaultBytesInterceptor());
    singleService.subscribe(this);
    singleService.close();
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