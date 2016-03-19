package com.ak.comm.serial;

import java.nio.ByteBuffer;

import com.ak.util.FinalizerGuardian;
import rx.Observable;
import rx.subjects.PublishSubject;

abstract class AbstractSerialService implements SerialService {
  private final Object finalizerGuardian = new FinalizerGuardian(this::close);
  private final PublishSubject<ByteBuffer> byteBufferPublish = PublishSubject.create();
  private String portName;

  public final void setPortName(String portName) {
    this.portName = portName;
  }

  @Override
  public final String getPortName() {
    return portName;
  }

  @Override
  public final Observable<ByteBuffer> getBufferObservable() {
    return byteBufferPublish.asObservable();
  }

  final PublishSubject<ByteBuffer> bufferPublish() {
    return byteBufferPublish;
  }
}
