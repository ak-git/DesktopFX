package com.ak.comm.core;

import java.io.Closeable;

import rx.Observable;

public interface Service<FROM> extends Closeable {
  Observable<FROM> getBufferObservable();

  @Override
  void close();
}
