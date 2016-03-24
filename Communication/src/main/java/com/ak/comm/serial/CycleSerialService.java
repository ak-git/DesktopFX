package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.comm.core.AbstractService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.UIConstants;
import rx.Observer;
import rx.Subscription;

public final class CycleSerialService<FROM, TO> extends AbstractService<FROM> {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final BytesInterceptor<FROM, TO> bytesInterceptor;
  private volatile SerialService serialService;

  public CycleSerialService(int baudRate, BytesInterceptor<FROM, TO> bytesInterceptor) {
    serialService = new SerialService(baudRate);
    this.bytesInterceptor = bytesInterceptor;
    bytesInterceptor.getBufferObservable().subscribe(bufferPublish());
    executor.scheduleAtFixedRate(() -> {
      AtomicBoolean workingFlag = new AtomicBoolean();
      CountDownLatch latch = new CountDownLatch(1);
      Subscription subscription = serialService.getBufferObservable().subscribe(new Observer<ByteBuffer>() {
        @Override
        public void onCompleted() {
          Logger.getLogger(getClass().getName()).config("Close connection " + serialService);
        }

        @Override
        public void onError(Throwable e) {
          bufferPublish().onError(e);
          workingFlag.set(false);
          latch.countDown();
        }

        @Override
        public void onNext(ByteBuffer buffer) {
          workingFlag.set(true);
          bytesInterceptor.write(buffer);
        }
      });

      if (bytesInterceptor.getStartCommand() == null || write(bytesInterceptor.getStartCommand()) > 0) {
        do {
          try {
            latch.await(UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
          }
          catch (InterruptedException e) {
            Logger.getLogger(getClass().getName()).log(Level.FINE, e.getMessage(), e);
            Thread.currentThread().interrupt();
            break;
          }
        }
        while (workingFlag.getAndSet(false));
      }

      synchronized (executor) {
        if (!workingFlag.get() && !executor.isShutdown()) {
          serialService.close();
          subscription.unsubscribe();
          serialService = new SerialService(baudRate);
        }
      }
    }, 0, UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
  }

  int write(TO to) {
    return serialService.write(bytesInterceptor.put(to));
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