package com.ak.comm.serial;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.core.AbstractService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.UIConstants;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Subscriber;

public final class CycleSerialService<RESPONSE, REQUEST> extends AbstractService<RESPONSE> {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  @Nonnull
  private final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor;
  @Nonnull
  private volatile SerialService serialService;

  public CycleSerialService(@Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor) {
    this.bytesInterceptor = bytesInterceptor;
    serialService = new SerialService(bytesInterceptor.getBaudRate());
  }

  @Override
  public void subscribe(Subscriber<? super RESPONSE> s) {
    s.onSubscribe(this);
    executor.scheduleAtFixedRate(() -> {
      AtomicBoolean workingFlag = new AtomicBoolean();
      AtomicReference<Instant> okTime = new AtomicReference<>(Instant.now());
      CountDownLatch latch = new CountDownLatch(1);

      Disposable disposable = Flowable.fromPublisher(serialService).doFinally(() -> {
        workingFlag.set(false);
        latch.countDown();
      }).flatMapIterable(buffer -> () -> bytesInterceptor.apply(buffer).iterator()).doOnNext(response -> {
        s.onNext(response);
        workingFlag.set(true);
        okTime.set(Instant.now());
      }).subscribe();

      while (!Thread.currentThread().isInterrupted()) {
        if (serialService.isOpen() && write(bytesInterceptor.getPingRequest()) == 0) {
          break;
        }
        else {
          okTime.set(Instant.now());
          try {
            while (Duration.between(okTime.get(), Instant.now()).minus(UIConstants.UI_DELAY).isNegative()) {
              latch.await(UIConstants.UI_DELAY.toMillis() / 10, TimeUnit.MILLISECONDS);
            }
          }
          catch (InterruptedException e) {
            Logger.getLogger(getClass().getName()).log(Level.FINEST, serialService.toString(), e);
            Thread.currentThread().interrupt();
            break;
          }

          if (!workingFlag.getAndSet(false)) {
            break;
          }
        }
      }

      synchronized (this) {
        if (!executor.isShutdown()) {
          disposable.dispose();
          serialService = new SerialService(bytesInterceptor.getBaudRate());
        }
      }
    }, 0, UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
  }

  @Override
  public void request(long n) {
  }

  @Override
  public void cancel() {
    synchronized (this) {
      serialService.close();
      executor.shutdownNow();
    }
  }

  public int write(@Nullable REQUEST request) {
    synchronized (this) {
      return request == null ? -1 : serialService.write(bytesInterceptor.putOut(request));
    }
  }
}