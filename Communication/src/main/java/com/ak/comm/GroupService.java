package com.ak.comm;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import com.ak.digitalfilter.IntsAcceptor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public final class GroupService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable> extends AbstractService
    implements FileFilter {
  @Nonnull
  private final CycleSerialService<RESPONSE, REQUEST, EV> serialService;

  @Inject
  public GroupService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider,
                      @Nonnull Provider<Converter<RESPONSE, EV>> converterProvider) {
    serialService = new CycleSerialService<>(interceptorProvider.get(), converterProvider.get());
  }

  @Override
  public boolean accept(File file) {
    throw new UnsupportedOperationException(file.toString());
  }

  public void forEach(@Nonnull IntsAcceptor acceptor) {
    serialService.subscribe(new Subscriber<int[]>() {
      @Override
      public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(int[] ints) {
        acceptor.accept(ints);
      }

      @Override
      public void onError(Throwable t) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, t.getMessage(), t);
      }

      @Override
      public void onComplete() {
      }
    });
  }

  @Override
  public void close() {
    serialService.close();
  }
}