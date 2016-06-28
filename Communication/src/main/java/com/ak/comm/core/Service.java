package com.ak.comm.core;

import java.io.Closeable;

import javax.annotation.Nonnull;

import rx.Observable;

public interface Service<RESPONSE> extends Closeable {
  @Nonnull
  Observable<RESPONSE> getBufferObservable();

  @Override
  void close();
}
