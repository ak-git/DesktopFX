package com.ak.comm.file;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.core.Readable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.Extension;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AutoFileReadingService<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractService<int[]> implements FileFilter, Readable {
  private static final Readable EMPTY_READABLE = (ignoreDst, ignorePosition) -> {
  };

  private final ExecutorService service = Executors.newSingleThreadExecutor();
  private final Supplier<BytesInterceptor<T, R>> interceptorProvider;
  private final Supplier<Converter<R, V>> converterProvider;
  private Flow.@Nullable Subscriber<? super int[]> subscriber;
  private Readable readable = EMPTY_READABLE;

  public AutoFileReadingService(Supplier<BytesInterceptor<T, R>> interceptorProvider, Supplier<Converter<R, V>> converterProvider) {
    this.interceptorProvider = Objects.requireNonNull(interceptorProvider);
    this.converterProvider = Objects.requireNonNull(converterProvider);
  }

  @Override
  public void subscribe(Flow.Subscriber<? super int[]> subscriber) {
    this.subscriber = Objects.requireNonNull(subscriber);
  }

  @Override
  public boolean accept(File file) {
    BytesInterceptor<T, R> bytesInterceptor = interceptorProvider.get();
    if (file.isFile() && Extension.BIN.is(file.getName()) && Extension.BIN.clean(file.getName()).contains(bytesInterceptor.name())) {
      refresh(false);
      CompletableFuture
          .supplyAsync(() -> Optional.of(new FileReadingService<>(file.toPath(), bytesInterceptor, converterProvider.get())))
          .whenComplete((source, throwable) -> {
            if (throwable != null) {
              Logger.getLogger(AutoFileReadingService.class.getName()).log(Level.WARNING, file.getName(), throwable);
            }
            else if (subscriber != null && source.isPresent()) {
              service.submit(() -> source.get().subscribe(subscriber));
            }
          })
          .exceptionally(ignore -> Optional.empty())
          .join()
          .ifPresentOrElse(readingService -> readable = readingService, () -> readable = EMPTY_READABLE);
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
    readable.refresh(force);
    readable = EMPTY_READABLE;
  }

  @Override
  public void read(ByteBuffer dst, long position) {
    readable.read(dst, position);
  }
}
