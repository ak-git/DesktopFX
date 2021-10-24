package com.ak.fx.desktop;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;

public abstract class AbstractScheduledViewController<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractViewController<T, R, V> implements Supplier<T> {
  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  @Nonnegative
  private final double frequencyHz;
  private final Runnable command = () -> {
    try {
      service().write(get());
    }
    catch (Exception e) {
      onError(e);
    }
  };
  @Nullable
  private ScheduledFuture<?> scheduledFuture;


  @ParametersAreNonnullByDefault
  protected AbstractScheduledViewController(Provider<BytesInterceptor<T, R>> interceptorProvider,
                                            Provider<Converter<R, V>> converterProvider,
                                            @Nonnegative double frequencyHz) {
    super(interceptorProvider, converterProvider);
    this.frequencyHz = frequencyHz;
  }

  @Override
  public final void onSubscribe(@Nonnull Flow.Subscription s) {
    super.onSubscribe(s);
    innerRefresh();
  }

  @Override
  public void refresh(boolean force) {
    super.refresh(force);
    innerRefresh();
  }

  @Override
  public final void close() throws IOException {
    innerCancel();
    executorService.shutdownNow();
    super.close();
  }

  private void innerRefresh() {
    innerCancel();
    scheduledFuture = executorService.scheduleAtFixedRate(command, 0, Math.round(1000 / frequencyHz), TimeUnit.MILLISECONDS);
  }

  private void innerCancel() {
    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
    }
  }
}
