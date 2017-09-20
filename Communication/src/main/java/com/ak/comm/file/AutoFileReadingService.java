package com.ak.comm.file;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.core.Readable;
import com.ak.comm.interceptor.BytesInterceptor;
import io.reactivex.Flowable;
import io.reactivex.internal.util.EmptyComponent;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public final class AutoFileReadingService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>>
    extends AbstractService implements FileFilter, Readable, Publisher<int[]> {
  @Nonnull
  private final Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider;
  @Nonnull
  private final Provider<Converter<RESPONSE, EV>> converterProvider;
  @Nonnull
  private Subscriber<? super int[]> subscriber = EmptyComponent.asSubscriber();
  @Nonnull
  private Readable readable = Readable.EMPTY_READABLE;

  public AutoFileReadingService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider,
                                @Nonnull Provider<Converter<RESPONSE, EV>> converterProvider) {
    this.interceptorProvider = interceptorProvider;
    this.converterProvider = converterProvider;
  }

  @Override
  public void subscribe(@Nonnull Subscriber<? super int[]> subscriber) {
    this.subscriber = subscriber;
  }

  @Override
  public boolean accept(@Nonnull File file) {
    if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
      close();
      FileReadingService<RESPONSE, REQUEST, EV> source = new FileReadingService<>(file.toPath(),
          interceptorProvider.get(), converterProvider.get()
      );
      readable = source;
      Flowable.fromPublisher(source).subscribeOn(Schedulers.io()).subscribe(subscriber);
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  public void close() {
    readable = Readable.EMPTY_READABLE;
  }

  @Override
  public void read(@Nonnull ByteBuffer dst, long position) {
    readable.read(dst, position);
  }
}
