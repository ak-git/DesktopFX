package com.ak.comm.file;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.digitalfilter.IntsAcceptor;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.EmptyComponent;
import io.reactivex.schedulers.Schedulers;

public final class AutoFileReadingService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>> extends AbstractService implements FileFilter {
  @Nonnull
  private final Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider;
  @Nonnull
  private final Provider<Converter<RESPONSE, EV>> converterProvider;
  @Nonnull
  private IntsAcceptor acceptor = IntsAcceptor.EMPTY_INTS_ACCEPTOR;
  @Nonnull
  private Disposable subscription = EmptyComponent.INSTANCE;

  public AutoFileReadingService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider,
                                @Nonnull Provider<Converter<RESPONSE, EV>> converterProvider) {
    this.interceptorProvider = interceptorProvider;
    this.converterProvider = converterProvider;
  }

  @Override
  public boolean accept(@Nonnull File file) {
    if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
      close();
      subscription = Flowable.fromPublisher(new FileReadingService<>(file.toPath(), interceptorProvider.get(), converterProvider.get())).
          subscribeOn(Schedulers.io()).subscribe(acceptor::accept,
          t -> Logger.getLogger(getClass().getName()).log(Level.WARNING, t.getMessage(), t));
      return true;
    }
    else {
      return false;
    }
  }

  public void subscribe(@Nonnull IntsAcceptor acceptor) {
    this.acceptor = acceptor;
  }

  @Override
  public void close() {
    subscription.dispose();
  }
}
