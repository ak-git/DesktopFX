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
import rx.Subscription;

public final class CycleSerialService<RESPONSE, REQUEST> extends AbstractService<RESPONSE> {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor;
  @Nonnull
  private volatile SerialService serialService;

  public CycleSerialService(@Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor) {
    serialService = new SerialService(bytesInterceptor);
    this.bytesInterceptor = bytesInterceptor;
    bytesInterceptor.getBufferObservable().subscribe(bufferPublish());
    executor.scheduleAtFixedRate(() -> {
      AtomicBoolean workingFlag = new AtomicBoolean();
      AtomicReference<Instant> okTime = new AtomicReference<>(Instant.now());
      CountDownLatch latch = new CountDownLatch(1);
      Subscription serviceSubscription = serialService.getBufferObservable().subscribe(
          buffer -> {
            if (bytesInterceptor.write(buffer) > 0) {
              workingFlag.set(true);
              okTime.set(Instant.now());
            }
          },
          throwable -> {
            workingFlag.set(false);
            latch.countDown();
          }
      );

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
            Logger.getLogger(getClass().getName()).log(Level.FINE, serialService.toString(), e);
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
          serialService.close();
          serviceSubscription.unsubscribe();
          serialService = new SerialService(bytesInterceptor);
        }
      }
    }, 0, UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
  }

  public int write(@Nullable REQUEST request) {
    return request == null ? -1 : serialService.write(bytesInterceptor.put(request));
  }

  @Override
  public void close() {
    synchronized (this) {
      executor.shutdownNow();
      serialService.close();
      bytesInterceptor.close();
    }
  }
}