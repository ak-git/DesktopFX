package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ak.comm.core.Service;
import com.ak.comm.interceptor.DefaultBytesInterceptor;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.testng.Assert;
import org.testng.annotations.Test;
import rx.Observer;

public final class SerialServiceTest implements Observer<ByteBuffer> {
  @Test
  public void test() {
    List<Service<ByteBuffer>> services = Stream.of(SerialPortList.getPortNames()).map(port -> {
      SerialService serialService = new SerialService(new DefaultBytesInterceptor());
      serialService.getBufferObservable().subscribe(this);
      Assert.assertEquals(serialService.write(ByteBuffer.allocate(0)), 0);
      return serialService;
    }).collect(Collectors.toList());

    Service<ByteBuffer> singleService = new SerialService(new DefaultBytesInterceptor());
    singleService.getBufferObservable().subscribe(this);
    singleService.close();
    services.forEach(Service::close);
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