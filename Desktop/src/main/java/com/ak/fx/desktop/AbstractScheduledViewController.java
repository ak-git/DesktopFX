package com.ak.fx.desktop;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;
import jakarta.inject.Provider;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.function.Supplier;

public abstract class AbstractScheduledViewController<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractViewController<T, R, V> implements Supplier<T> {
  private final ScheduledExecutorService pingService = Executors.newSingleThreadScheduledExecutor();
  private final double frequencyHz;
  private final Runnable command = () -> {
    try {
      service().write(get());
    }
    catch (Exception e) {
      onError(e);
    }
  };
  private @Nullable ScheduledFuture<?> scheduledFuture;

  protected AbstractScheduledViewController(Provider<BytesInterceptor<T, R>> interceptorProvider,
                                            Provider<Converter<R, V>> converterProvider,
                                            double frequencyHz) {
    super(interceptorProvider, converterProvider);
    this.frequencyHz = frequencyHz;
  }

  @Override
  public final void onSubscribe(Flow.Subscription s) {
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
    pingService.shutdownNow();
    super.close();
  }

  private void innerRefresh() {
    innerCancel();
    scheduledFuture = pingService.scheduleWithFixedDelay(command, 0, Math.round(1000 / frequencyHz), TimeUnit.MILLISECONDS);
  }

  private void innerCancel() {
    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
    }
  }
}
