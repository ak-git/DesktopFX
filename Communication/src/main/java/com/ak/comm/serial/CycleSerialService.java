package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.util.UIConstants;
import rx.Observer;
import rx.Subscription;

public final class CycleSerialService extends AbstractSerialService {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private volatile SerialService serialService;

  public CycleSerialService(int baudRate) {
    serialService = new SingleSerialService(baudRate);
    executor.scheduleWithFixedDelay(() -> {
      AtomicBoolean workingFlag = new AtomicBoolean();
      CountDownLatch latch = new CountDownLatch(1);
      Subscription subscription = serialService.getBufferObservable().subscribe(new Observer<ByteBuffer>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
          bufferPublish().onError(e);
          workingFlag.set(false);
          latch.countDown();
        }

        @Override
        public void onNext(ByteBuffer buffer) {
          bufferPublish().onNext(buffer);
          workingFlag.set(true);
        }
      });

      if (isWrite(new byte[] {0x7E, (byte) 0x81, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07})) {
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
        while (workingFlag.get());
      }

      if (!workingFlag.get()) {
        Logger.getLogger(getClass().getName()).config("Change serial connection");
        serialService.close();
        subscription.unsubscribe();
        serialService = new SingleSerialService(baudRate);
      }
    }, 0, UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
  }

  @Override
  public boolean isWrite(byte[] bytes) {
    return serialService.isWrite(bytes);
  }

  @Override
  public void close() {
    executor.shutdownNow();
    serialService.close();
    bufferPublish().onCompleted();
  }
}