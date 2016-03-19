package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.util.FinalizerGuardian;
import com.ak.util.UIConstants;
import rx.Observer;

public final class CycleSerialService implements AutoCloseable {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final Object finalizerGuardian = new FinalizerGuardian(this::close);
  private volatile SingleSerialService serialService;

  public CycleSerialService(int baudRate) {
    serialService = new SingleSerialService(baudRate);
    executor.scheduleWithFixedDelay(() -> {
      AtomicBoolean workingFlag = new AtomicBoolean();
      CountDownLatch latch = new CountDownLatch(1);
      serialService.getBufferObservable().subscribe(new Observer<ByteBuffer>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
          workingFlag.set(false);
          latch.countDown();
        }

        @Override
        public void onNext(ByteBuffer buffer) {
          workingFlag.set(true);
        }
      });

      if (serialService.isWrite(new byte[] {0x7E, (byte) 0x81, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07})) {
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
        serialService.close();
        serialService = new SingleSerialService(baudRate);
      }
    }, 0, UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
  }

  @Override
  public void close() {
    executor.shutdownNow();
    serialService.close();
  }
}