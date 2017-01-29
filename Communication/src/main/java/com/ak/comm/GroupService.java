package com.ak.comm;

import java.io.File;
import java.io.FileFilter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public final class GroupService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable> extends AbstractService
    implements FileFilter, Publisher<int[]> {
  @Nonnull
  private final Flowable<int[]> serialFlow;
  @Nonnull
  private final CycleSerialService<RESPONSE, REQUEST, EV> serialService;

  @Inject
  public GroupService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider,
                      @Nonnull Provider<Converter<RESPONSE, EV>> converterProvider) {
    serialService = new CycleSerialService<>(interceptorProvider.get(), converterProvider.get());
    serialFlow = Flowable.fromPublisher(serialService);
  }

  @Override
  public boolean accept(File file) {
    throw new UnsupportedOperationException(file.toString());
  }

  @Override
  public void subscribe(Subscriber<? super int[]> s) {
    serialFlow.subscribe(s);
  }

  @Override
  public void close() {
    serialService.close();
  }
}