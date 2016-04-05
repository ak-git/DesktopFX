package com.ak.comm.core;

import java.io.Closeable;

import rx.Observable;

public interface Service<RESPONSE> extends Closeable {
  Observable<RESPONSE> getBufferObservable();

  @Override
  void close();
}
