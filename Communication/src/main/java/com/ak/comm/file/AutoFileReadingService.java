package com.ak.comm.file;

import java.io.File;
import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.core.Readable;
import com.ak.comm.interceptor.BytesInterceptor;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;

public final class AutoFileReadingService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>>
    extends AbstractService implements Readable {
  @Nonnull
  private final Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider;
  @Nonnull
  private final Provider<Converter<RESPONSE, EV>> converterProvider;
  @Nonnull
  private Readable readable = Readable.EMPTY_READABLE;

  public AutoFileReadingService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider,
                                @Nonnull Provider<Converter<RESPONSE, EV>> converterProvider) {
    this.interceptorProvider = interceptorProvider;
    this.converterProvider = converterProvider;
  }

  public boolean isAccept(@Nonnull File file, Subscriber<int[]> subscriber) {
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
