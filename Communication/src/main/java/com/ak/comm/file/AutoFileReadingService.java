package com.ak.comm.file;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.BytesInterceptor;
import io.reactivex.Flowable;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.internal.util.EmptyComponent;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public final class AutoFileReadingService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>>
    extends AbstractConvertableService<RESPONSE, REQUEST, EV> implements FileFilter, SingleSource<Path> {
  @Nonnull
  private volatile SingleObserver<? super Path> observer = EmptyComponent.INSTANCE;
  @Nonnull
  private volatile FileReadingService fileReadingService = new FileReadingService(Paths.get(""));

  public AutoFileReadingService(@Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor,
                                @Nonnull Converter<RESPONSE, EV> responseConverter) {
    super(bytesInterceptor, responseConverter);
  }

  @Override
  public void subscribe(SingleObserver<? super Path> observer) {
    this.observer = observer;
  }

  @Override
  public boolean accept(@Nonnull File file) {
    if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
      close();
      fileReadingService = new FileReadingService(file.toPath());
      Flowable.fromPublisher(fileReadingService).subscribeOn(Schedulers.io()).
          flatMapIterable(buffer -> () -> process(buffer).iterator()).subscribe(new Subscriber<int[]>() {
        @Override
        public void onSubscribe(Subscription s) {
          s.request(Long.MAX_VALUE);
          observer.onSubscribe(fileReadingService);
        }

        @Override
        public void onNext(int[] ints) {
        }

        @Override
        public void onError(Throwable t) {
          observer.onError(t);
        }

        @Override
        public void onComplete() {
          close();
          observer.onSuccess(getPath());
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
    try {
      fileReadingService.close();
    }
    finally {
      super.close();
    }
  }
}