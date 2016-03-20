package com.ak.comm.core;

import java.io.Closeable;

import rx.Observable;

public interface Service<T> extends Closeable {
  Observable<T> getBufferObservable();

  @Override
  void close();
}
