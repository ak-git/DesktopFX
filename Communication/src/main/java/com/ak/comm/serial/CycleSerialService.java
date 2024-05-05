package com.ak.comm.serial;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.LogBuilders;
import com.ak.util.UIConstants;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CycleSerialService<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractConvertableService<T, R, V> implements Flow.Subscription {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private volatile boolean cancelled;
  private SerialService<T, R> serialService;

  public CycleSerialService(BytesInterceptor<T, R> bytesInterceptor, Converter<R, V> responseConverter) {
    super(bytesInterceptor, responseConverter);
    serialService = new SerialService<>(bytesInterceptor);
  }

  @Override
  public void subscribe(Flow.Subscriber<? super int[]> s) {
    s.onSubscribe(this);
    executor.scheduleWithFixedDelay(() -> {
      AliveSubscriber subscriber = new AliveSubscriber(s);
      serialService.subscribe(subscriber);

      while (serialService.isOpen() && bytesInterceptor().getPingRequest().map(r -> write(r) != 0).orElse(Boolean.TRUE)) {
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

  public int write(T request) {
    return serialService.write(bytesInterceptor().putOut(request));
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
    bytesInterceptor().getPingRequest().ifPresent(this::write);
  }

  @Override
  public void request(long n) {
    serialService.request(n);
  }

  @Override
  public void cancel() {
    cancelled = true;
  }

  private final class AliveSubscriber implements Flow.Subscriber<ByteBuffer> {
    private final AtomicBoolean aliveFlag = new AtomicBoolean();
    private final AtomicReference<Instant> okTime = new AtomicReference<>(Instant.now());
    private final CountDownLatch latch = new CountDownLatch(1);
    private final Flow.Subscriber<? super int[]> subscriber;
    @Nullable
    private Flow.Subscription subscription;

    private AliveSubscriber(Flow.Subscriber<? super int[]> subscriber) {
      this.subscriber = Objects.requireNonNull(subscriber);
    }

    @Override
    public void onSubscribe(Flow.Subscription s) {
      subscription = Objects.requireNonNull(s);
    }

    @Override
    public void onNext(ByteBuffer buffer) {
      process(buffer, ints -> {
        if (!cancelled) {
          subscriber.onNext(ints);
        }
        aliveFlag.set(true);
        okTime.set(Instant.now());
      });
    }

    @Override
    public void onError(Throwable throwable) {
      serialService.close();
      Logger.getLogger(CycleSerialService.class.getName()).log(Level.SEVERE, serialService.toString(), throwable);
    }

    @Override
    public void onComplete() {
      try {
        aliveFlag.set(false);
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
        aliveFlag.set(false);
      }
      return !aliveFlag.getAndSet(false) || Thread.currentThread().isInterrupted();
    }
  }
}