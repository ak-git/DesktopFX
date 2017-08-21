package com.ak.comm.file;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.core.SafeByteChannel;
import com.ak.comm.interceptor.BytesInterceptor;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.EmptyComponent;
import io.reactivex.schedulers.Schedulers;

public final class AutoFileReadingService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>>
    extends AbstractService implements FileFilter {
  @Nonnull
  private final Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider;
  @Nonnull
  private final Provider<Converter<RESPONSE, EV>> converterProvider;
  @Nonnull
  private Disposable subscription = EmptyComponent.INSTANCE;
  @Nonnull
  private final Lock lock = new ReentrantLock();
  @Nonnull
  private final Condition notFull = lock.newCondition();
  @Nonnull
  private final Condition notEmpty = lock.newCondition();
  @Nonnull
  private volatile boolean full;
  @Nonnull
  private volatile boolean empty = true;
  @Nonnull
  private volatile SeekableByteChannel valuesByteChannel = SafeByteChannel.EMPTY_CHANNEL;

  public AutoFileReadingService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider,
                                @Nonnull Provider<Converter<RESPONSE, EV>> converterProvider) {
    this.interceptorProvider = interceptorProvider;
    this.converterProvider = converterProvider;
  }

  @Override
  public boolean accept(@Nonnull File file) {
    if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
      close();
      FileReadingService<RESPONSE, REQUEST, EV> fileReadingService = new FileReadingService<>(file.toPath(),
          interceptorProvider.get(), converterProvider.get());
      subscription = Flowable.fromPublisher(fileReadingService).
          subscribeOn(Schedulers.io()).subscribe(values -> {
            lock.lock();
            try {
              empty = false;
              notEmpty.signalAll();
              valuesByteChannel = fileReadingService.call();
              while (full) {
                notFull.await();
              }
              empty = true;
            }
            catch (InterruptedException e) {
              Logger.getLogger(getClass().getName()).log(Level.CONFIG, e.getMessage(), e);
            }
            finally {
              lock.unlock();
            }
          },
          t -> Logger.getLogger(getClass().getName()).log(Level.WARNING, t.getMessage(), t),
          () -> {
            lock.lock();
            try {
              valuesByteChannel = fileReadingService.call();
              full = false;
              empty = false;
              notEmpty.signalAll();
            }
            finally {
              lock.unlock();
            }
          });
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  public void close() {
    subscription.dispose();
  }

  int read(@Nonnegative int position, @Nonnull ByteBuffer destination) {
    int bytesCount = 0;
    lock.lock();
    try {
      full = true;
      while (empty) {
        notEmpty.await();
      }

      long oldPos = valuesByteChannel.position();
      valuesByteChannel.position(position);
      bytesCount = valuesByteChannel.read(destination);
      valuesByteChannel.position(oldPos);

      full = false;
      notFull.signalAll();
    }
    catch (InterruptedException e) {
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, e.getMessage(), e);
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
    }
    finally {
      lock.unlock();
    }
    return bytesCount;
  }
}
