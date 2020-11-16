package com.ak.fx.desktop;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Provider;

import com.ak.comm.GroupService;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;

public abstract class AbstractScheduledViewController<T, R, V extends Enum<V> & Variable<V>> extends AbstractViewController<T, R, V> {
  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  protected AbstractScheduledViewController(@Nonnull Provider<BytesInterceptor<T, R>> interceptorProvider,
                                            @Nonnull Provider<Converter<R, V>> converterProvider,
                                            @Nonnull Supplier<T> writeRequest,
                                            @Nonnegative double frequencyHz) {
    super(new GroupService<>(interceptorProvider::get, converterProvider::get));
    executorService.scheduleAtFixedRate(() -> service().write(writeRequest.get()),
        0, Math.round(1000 / frequencyHz), TimeUnit.MILLISECONDS);

  }

  @Override
  public final void close() {
    executorService.shutdownNow();
    super.close();
  }
}
