package com.ak.comm.serial;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.LogBuilders;
import com.ak.util.UIConstants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CycleSerialService<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractConvertableService<T, R, V> implements Flow.Subscription {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private volatile boolean cancelled;
  @Nonnull
  private SerialService<T, R> serialService;

  @ParametersAreNonnullByDefault
  public CycleSerialService(BytesInterceptor<T, R> bytesInterceptor, Converter<R, V> responseConverter) {
    super(bytesInterceptor, responseConverter);
    serialService = new SerialService<>(bytesInterceptor);
  }

  @Override
  public void subscribe(@Nonnull Flow.Subscriber<? super int[]> s) {
    s.onSubscribe(this);
    executor.scheduleWithFixedDelay(() -> {
      SerialSubscriber subscriber = new SerialSubscriber(s);
      serialService.subscribe(subscriber);

      while (serialService.isOpen() && write(bytesInterceptor().getPingRequest()) != 0) {
        if (subscriber.isBreak()) {
          break;
        }
      }

      if (!executor.isShutdown()) {
        subscriber.onComplete();
        serialService = new SerialService<>(bytesInterceptor());
      }
    }, 1, UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
  }

  @Override
  public void close() {
    try {
      executor.shutdownNow();
      serialService.close();
    }
    finally {
      super.close();
    }
  }

  public int write(@Nullable T request) {
    return request == null ? -1 : serialService.write(bytesInterceptor().putOut(request));
  }

  @Override
  public AsynchronousFileChannel call() throws IOException {
    var path = LogBuilders.CONVERTER_SERIAL.build("#%08x".formatted(hashCode())).getPath();
    return AsynchronousFileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
  }

  @Override
  public void refresh(boolean force) {
    serialService.refresh(force);
    cancelled = false;
    write(bytesInterceptor().getPingRequest());
  }

  @Override
  public void request(long n) {
    serialService.request(n);
  }

  @Override
  public void cancel() {
    cancelled = true;
  }

  private final class SerialSubscriber implements Flow.Subscriber<ByteBuffer> {
    private final AtomicBoolean workingFlag = new AtomicBoolean();
    private final AtomicReference<Instant> okTime = new AtomicReference<>(Instant.now());
    private final CountDownLatch latch = new CountDownLatch(1);
    private final Flow.Subscriber<? super int[]> subscriber;
    @Nullable
    private Flow.Subscription subscription;

    private SerialSubscriber(@Nonnull Flow.Subscriber<? super int[]> subscriber) {
      this.subscriber = subscriber;
    }

    @Override
    public void onSubscribe(@Nonnull Flow.Subscription s) {
      subscription = s;
    }

    @Override
    public void onNext(@Nonnull ByteBuffer buffer) {
      process(buffer, ints -> {
        if (!cancelled) {
          subscriber.onNext(ints);
        }
        workingFlag.set(true);
        okTime.set(Instant.now());
      });
    }

    @Override
    public void onError(@Nonnull Throwable throwable) {
      serialService.close();
      Logger.getLogger(CycleSerialService.class.getName()).log(Level.SEVERE, serialService.toString(), throwable);
    }

    @Override
    public void onComplete() {
      try {
        workingFlag.set(false);
        latch.countDown();
        if (subscription != null) {
          subscription.cancel();
        }
      }
      catch (Exception e) {
        Logger.getLogger(CycleSerialService.class.getName()).log(Level.INFO, e.getMessage(), e);
      }
    }

    private boolean isBreak() {
      try {
        okTime.set(Instant.now());
        while (Duration.between(okTime.get(), Instant.now()).minus(UIConstants.UI_DELAY).isNegative()) {
          if (latch.await(UIConstants.UI_DELAY.toMillis(), TimeUnit.MILLISECONDS)) {
            break;
          }
        }
      }
      catch (InterruptedException e) {
        Logger.getLogger(getClass().getName()).log(Level.ALL, serialService.toString(), e);
        Thread.currentThread().interrupt();
        workingFlag.set(false);
      }
      return !workingFlag.getAndSet(false) || Thread.currentThread().isInterrupted();
    }
  }
}