package com.ak.comm.core;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.ak.comm.interceptor.BytesInterceptor;

public abstract class AbstractInterceptorService<RESPONSE, REQUEST> extends AbstractService<RESPONSE> {
  private final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor;

  public AbstractInterceptorService(@Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor) {
    this.bytesInterceptor = bytesInterceptor;
    bytesInterceptor.getBufferObservable().subscribe(bufferPublish());
  }

  @OverridingMethodsMustInvokeSuper
  @Override
  public void close() {
    bytesInterceptor.close();
    super.close();
  }

  @Nonnull
  protected BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor() {
    return bytesInterceptor;
  }
}