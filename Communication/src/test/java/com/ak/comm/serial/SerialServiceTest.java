package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

import com.ak.comm.interceptor.BytesInterceptor;
import jssc.SerialPortList;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SerialServiceTest implements Flow.Subscriber<ByteBuffer> {
  @Test
  public void test() {
    List<SerialService> services = Arrays.stream(SerialPortList.getPortNames()).map(port -> {
      SerialService serialService = new SerialService(115200, Collections.emptySet());
      serialService.subscribe(this);
      Assert.assertEquals(serialService.write(ByteBuffer.allocate(0)), 0);
      return serialService;
    }).collect(Collectors.toList());

    SerialService singleService = new SerialService(115200, EnumSet.of(BytesInterceptor.SerialParams.CLEAR_DTR));
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