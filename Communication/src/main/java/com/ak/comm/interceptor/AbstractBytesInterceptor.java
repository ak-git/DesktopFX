package com.ak.comm.interceptor;

import com.ak.comm.core.AbstractService;

public abstract class AbstractBytesInterceptor<T> extends AbstractService<T> implements BytesInterceptor<T> {
  @Override
  public final boolean isOpen() {
    return bufferPublish().hasObservers();
  }

  @Override
  public final void close() {
    bufferPublish().onCompleted();
  }
}
