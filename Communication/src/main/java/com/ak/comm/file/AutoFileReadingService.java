package com.ak.comm.file;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.core.Readable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.Extension;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AutoFileReadingService<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractService<int[]> implements FileFilter, Readable {
  private static final Readable EMPTY_READABLE = (dst, position) -> {
  };

  @Nonnull
  private final ExecutorService service = Executors.newSingleThreadExecutor();
  @Nonnull
  private final Supplier<BytesInterceptor<T, R>> interceptorProvider;
  @Nonnull
  private final Supplier<Converter<R, V>> converterProvider;
  @Nullable
  private Flow.Subscriber<? super int[]> subscriber;
  @Nonnull
  private Readable readable = EMPTY_READABLE;

  @ParametersAreNonnullByDefault
  public AutoFileReadingService(Supplier<BytesInterceptor<T, R>> interceptorProvider, Supplier<Converter<R, V>> converterProvider) {
    this.interceptorProvider = interceptorProvider;
    this.converterProvider = converterProvider;
  }

  @Override
  public void subscribe(@Nonnull Flow.Subscriber<? super int[]> subscriber) {
    this.subscriber = subscriber;
  }

  @Override
  public boolean accept(@Nonnull File file) {
    BytesInterceptor<T, R> bytesInterceptor = interceptorProvider.get();
    if (file.isFile() && Extension.BIN.is(file.getName()) && Extension.BIN.clean(file.getName()).endsWith(bytesInterceptor.name())) {
      refresh(false);
      readable = CompletableFuture
          .supplyAsync(() -> new FileReadingService<>(file.toPath(), bytesInterceptor, converterProvider.get()))
          .whenComplete((source, throwable) -> {
            if (throwable != null) {
              Logger.getLogger(FileReadingService.class.getName()).log(Level.WARNING, file.getName(), throwable);
            }
            else if (subscriber != null) {
              service.submit(() -> source.subscribe(subscriber));
            }
          })
          .join();
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  public void close() {
    refresh(false);
    service.shutdownNow();
  }

  @Override
  public void refresh(boolean force) {
    readable.close();
    readable = EMPTY_READABLE;
  }

  @Override
  public void read(@Nonnull ByteBuffer dst, long position) {
    readable.read(dst, position);
  }
}
