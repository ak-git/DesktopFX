package com.ak.comm;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.file.AutoFileReadingService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import com.ak.comm.serial.Refreshable;
import com.ak.digitalfilter.IntsAcceptor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public final class GroupService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>> extends AbstractService
    implements FileFilter, Refreshable {
  @Nonnull
  private final CycleSerialService<RESPONSE, REQUEST, EV> serialService;
  @Nonnull
  private final AutoFileReadingService<RESPONSE, REQUEST, EV> fileReadingService;
  @Nonnull
  private final List<EV> variables;

  @Inject
  public GroupService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider,
                      @Nonnull Provider<Converter<RESPONSE, EV>> converterProvider) {
    Converter<RESPONSE, EV> converter = converterProvider.get();
    variables = converter.variables();
    serialService = new CycleSerialService<>(interceptorProvider.get(), converter);
    fileReadingService = new AutoFileReadingService<>(interceptorProvider, converterProvider);
  }

  @Override
  public boolean accept(@Nonnull File file) {
    return fileReadingService.accept(file);
  }

  @Override
  public void refresh() {
    serialService.refresh();
  }

  public void subscribeSerial(@Nonnull IntsAcceptor acceptor) {
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

  public List<EV> getVariables() {
    return variables;
  }

  @Override
  public void close() throws IOException {
    serialService.close();
    fileReadingService.close();
  }
}