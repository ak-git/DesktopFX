package com.ak.comm.file;

import java.io.File;
import java.io.FileFilter;

import javax.annotation.Nonnull;

import com.ak.comm.converter.Converter;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.BytesInterceptor;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.EmptyComponent;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;

public final class AutoFileReadingService<RESPONSE, REQUEST> extends AbstractConvertableService<RESPONSE, REQUEST> implements FileFilter {
  @Nonnull
  private volatile Disposable subscription = EmptyComponent.INSTANCE;

  public AutoFileReadingService(@Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor,
                                @Nonnull Converter<RESPONSE> responseConverter) {
    super(bytesInterceptor, responseConverter);
  }

  @Override
  public void subscribe(Subscriber<? super int[]> s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void cancel() {
    subscription.dispose();
  }

  @Override
  public boolean accept(File file) {
    if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
      cancel();
      subscription = Flowable.fromPublisher(new FileReadingService(file.toPath())).subscribeOn(Schedulers.io()).
          flatMapIterable(buffer -> () -> process(buffer).iterator()).subscribe();
      return true;
    }
    else {
      return false;
    }
  }
}