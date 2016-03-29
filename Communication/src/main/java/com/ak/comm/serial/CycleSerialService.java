package com.ak.comm.serial;

import java.nio.ByteBuffer;
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

import com.ak.comm.core.AbstractService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.UIConstants;
import rx.Observer;
import rx.Subscription;

public final class CycleSerialService<RESPONSE, REQUEST> extends AbstractService<RESPONSE> {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor;
  private volatile SerialService serialService;

  public CycleSerialService(int baudRate, BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor) {
    serialService = new SerialService(baudRate);
    this.bytesInterceptor = bytesInterceptor;
    bytesInterceptor.getBufferObservable().subscribe(bufferPublish());
    executor.scheduleAtFixedRate(() -> {
      AtomicBoolean workingFlag = new AtomicBoolean();
      AtomicReference<Instant> okTime = new AtomicReference<>(Instant.now());
      CountDownLatch latch = new CountDownLatch(1);
      Subscription serviceSubscription = serialService.getBufferObservable().subscribe(new Observer<ByteBuffer>() {
        @Override
        public void onCompleted() {
          Logger.getLogger(getClass().getName()).config("Close connection " + serialService);
        }

        @Override
        public void onError(Throwable e) {
          workingFlag.set(false);
          latch.countDown();
        }

        @Override
        public void onNext(ByteBuffer buffer) {
          if (bytesInterceptor.write(buffer) > 0) {
            workingFlag.set(true);
            okTime.set(Instant.now());
          }
        }
      });

      while (!Thread.currentThread().isInterrupted()) {
        if (write(bytesInterceptor.getPingRequest()) == 0) {
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

      synchronized (executor) {
        if (!executor.isShutdown()) {
          serialService.close();
          serviceSubscription.unsubscribe();
          serialService = new SerialService(baudRate);
        }
      }
    }, 0, UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
  }

  public int write(REQUEST request) {
    return request == null ? -1 : serialService.write(bytesInterceptor.put(request));
  }

  @Override
  public void close() {
    synchronized (executor) {
      executor.shutdownNow();
      serialService.close();
      bytesInterceptor.close();
    }
  }
}