package com.ak.comm.serial;

import java.nio.ByteBuffer;

import rx.Observable;

public interface SerialService extends AutoCloseable {
  String getPortName();

  Observable<ByteBuffer> getBufferObservable();

  boolean isWrite(byte[] bytes);

  @Override
  void close();
}
