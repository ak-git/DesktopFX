package com.ak.comm.serial;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Refreshable;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.LogBuilders;
import com.ak.util.UIConstants;

public final class CycleSerialService<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractConvertableService<T, R, V> implements Refreshable, Flow.Subscription {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private volatile boolean cancelled;
  @Nonnull
  private SerialService serialService;

  public CycleSerialService(@Nonnull BytesInterceptor<T, R> bytesInterceptor,
                            @Nonnull Converter<R, V> responseConverter) {
    super(bytesInterceptor, responseConverter);
    serialService = new SerialService(bytesInterceptor.getBaudRate(), bytesInterceptor.getSerialParams());
  }

  @Override
  public void subscribe(@Nonnull Flow.Subscriber<? super int[]> s) {
    s.onSubscribe(this);
    executor.scheduleAtFixedRate(() -> {
      AtomicBoolean workingFlag = new AtomicBoolean();
      AtomicReference<Instant> okTime = new AtomicReference<>(Instant.now());
      CountDownLatch latch = new CountDownLatch(1);

      Flow.Subscriber<ByteBuffer> subscriber = new Flow.Subscriber<>() {
        @Nullable
        Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription s) {
          subscription = s;
        }

        @Override
        public void onNext(ByteBuffer buffer) {
          process(buffer).forEach(ints -> {
            if (!cancelled) {
              s.onNext(ints);
            }
            workingFlag.set(true);
            okTime.set(Instant.now());
          });
        }

        @Override
        public void onError(Throwable throwable) {
          synchronized (CycleSerialService.this) {
            serialService.close();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, serialService.toString(), throwable);
          }
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
            Logger.getLogger(getClass().getName()).log(Level.INFO, e.getMessage(), e);
          }
        }
      };

      synchronized (this) {
        serialService.subscribe(subscriber);

        while (serialService.isOpen() && write(bytesInterceptor().getPingRequest()) != 0) {
          okTime.set(Instant.now());
          try {
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

          if (!workingFlag.getAndSet(false) || Thread.currentThread().isInterrupted()) {
            break;
          }
        }

        if (!executor.isShutdown()) {
          subscriber.onComplete();
          serialService = new SerialService(bytesInterceptor().getBaudRate(), bytesInterceptor().getSerialParams());
        }
      }
    }, 0, UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
  }

  @Override
  public void close() {
    synchronized (this) {
      try {
        executor.shutdownNow();
        serialService.close();
      }
      finally {
        super.close();
      }
    }
  }

  public int write(@Nullable T request) {
    synchronized (this) {
      return request == null ? -1 : serialService.write(bytesInterceptor().putOut(request));
    }
  }

  @Override
  public AsynchronousFileChannel call() throws IOException {
    Path path = LogBuilders.CONVERTER_SERIAL.build(getClass().getSimpleName()).getPath();
    return AsynchronousFileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
  }

  @Override
  public void refresh() {
    synchronized (this) {
      serialService.refresh();
    }
    cancelled = false;
    write(bytesInterceptor().getPingRequest());
  }

  @Override
  public void request(long n) {
    synchronized (this) {
      serialService.request(n);
    }
  }

  @Override
  public void cancel() {
    cancelled = true;
  }
}