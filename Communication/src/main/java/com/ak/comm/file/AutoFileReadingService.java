package com.ak.comm.file;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.core.Readable;
import com.ak.comm.interceptor.BytesInterceptor;

public final class AutoFileReadingService<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractService implements FileFilter, Readable, Flow.Publisher<int[]> {
  @Nonnull
  private final ExecutorService service = Executors.newSingleThreadExecutor();
  @Nonnull
  private final Provider<BytesInterceptor<T, R>> interceptorProvider;
  @Nonnull
  private final Provider<Converter<R, V>> converterProvider;
  @Nullable
  private Flow.Subscriber<? super int[]> subscriber;
  @Nonnull
  private Readable readable = Readable.EMPTY_READABLE;

  public AutoFileReadingService(@Nonnull Provider<BytesInterceptor<T, R>> interceptorProvider,
                                @Nonnull Provider<Converter<R, V>> converterProvider) {
    this.interceptorProvider = interceptorProvider;
    this.converterProvider = converterProvider;
  }

  @Override
  public void subscribe(@Nonnull Flow.Subscriber<? super int[]> subscriber) {
    this.subscriber = subscriber;
  }

  @Override
  public boolean accept(@Nonnull File file) {
    if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
      innerClose();
      FileReadingService<T, R, V> source = new FileReadingService<>(file.toPath(),
          interceptorProvider.get(), converterProvider.get()
      );
      readable = source;
      if (subscriber != null) {
        service.submit(() -> source.subscribe(subscriber));
      }
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  public void close() {
    innerClose();
    service.shutdownNow();
  }

  @Override
  public void read(@Nonnull ByteBuffer dst, long position) {
    readable.read(dst, position);
  }

  private void innerClose() {
    readable.close();
    readable = Readable.EMPTY_READABLE;
  }
}
