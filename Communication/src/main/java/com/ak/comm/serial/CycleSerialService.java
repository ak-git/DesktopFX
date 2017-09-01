package com.ak.comm.serial;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.logging.LogBuilders;
import com.ak.util.UIConstants;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public final class CycleSerialService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>>
    extends AbstractConvertableService<RESPONSE, REQUEST, EV> implements Publisher<int[]>, Refreshable {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  @Nonnull
  private volatile SerialService serialService;

  public CycleSerialService(@Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor,
                            @Nonnull Converter<RESPONSE, EV> responseConverter) {
    super(bytesInterceptor, responseConverter);
    serialService = new SerialService(bytesInterceptor.getBaudRate());
  }

  @Override
  public void subscribe(@Nonnull Subscriber<? super int[]> s) {
    s.onSubscribe(this);
    executor.scheduleAtFixedRate(() -> {
      AtomicBoolean workingFlag = new AtomicBoolean();
      AtomicReference<Instant> okTime = new AtomicReference<>(Instant.now());
      CountDownLatch latch = new CountDownLatch(1);

      Disposable disposable = Flowable.fromPublisher(serialService).doFinally(() -> {
        workingFlag.set(false);
        latch.countDown();
      }).subscribe(buffer -> process(buffer).forEach(ints -> {
        s.onNext(ints);
        workingFlag.set(true);
        okTime.set(Instant.now());
      }), throwable -> {
        serialService.close();
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, serialService.toString(), throwable);
      });

      while (!Thread.currentThread().isInterrupted()) {
        if (!serialService.isOpen() || write(bytesInterceptor().getPingRequest()) == 0) {
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
            Logger.getLogger(getClass().getName()).log(Level.ALL, serialService.toString(), e);
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
          serialService = new SerialService(bytesInterceptor().getBaudRate());
        }
      }
    }, 0, UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
  }

  @Override
  public void close() throws IOException {
    synchronized (this) {
      try {
        serialService.close();
        executor.shutdownNow();
      }
      finally {
        super.close();
      }
    }
  }

  public int write(@Nullable REQUEST request) {
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
    serialService.refresh();
    write(bytesInterceptor().getPingRequest());
  }
}