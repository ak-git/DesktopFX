package com.ak.comm.serial;

import java.nio.ByteBuffer;

import org.testng.annotations.Test;
import rx.Observer;

public final class CycleSerialServiceTest implements Observer<ByteBuffer> {
  @Test
  public void test() {
    SerialService service = new CycleSerialService(115200);
    service.getBufferObservable().subscribe(this);
    service.isWrite(new byte[] {0x7E});
    service.close();
  }

  @Override
  public void onCompleted() {
  }

  @Override
  public void onError(Throwable e) {
  }

  @Override
  public void onNext(ByteBuffer buffer) {
  }
}