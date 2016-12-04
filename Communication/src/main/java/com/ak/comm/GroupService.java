package com.ak.comm;

import java.io.File;
import java.io.FileFilter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.core.AbstractService;
import com.ak.comm.file.FileService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.EmptyComponent;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;

public final class GroupService<RESPONSE, REQUEST> extends AbstractService<int[]> implements FileFilter {
  @Nonnull
  private final Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider;
  @Nonnull
  private final Provider<Converter<RESPONSE>> converterProvider;
  @Nonnull
  private final Flowable<int[]> serialFlow;
  @Nonnull
  private Disposable fileSubscription = EmptyComponent.INSTANCE;

  @Inject
  public GroupService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider,
                      @Nonnull Provider<Converter<RESPONSE>> converterProvider) {
    this.interceptorProvider = interceptorProvider;
    this.converterProvider = converterProvider;
    serialFlow = Flowable.fromPublisher(new CycleSerialService<>(interceptorProvider.get(), converterProvider.get()));
  }

  @Override
  public boolean accept(File file) {
    if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
      fileSubscription.dispose();

      BytesInterceptor<RESPONSE, REQUEST> interceptor = interceptorProvider.get();
      Converter<RESPONSE> converter = converterProvider.get();
      fileSubscription = Flowable.fromPublisher(new FileService(file.toPath())).subscribeOn(Schedulers.io()).
          flatMapIterable(buffer -> () -> interceptor.apply(buffer).iterator()).
          flatMapIterable(response -> () -> converter.apply(response).iterator()).subscribe();
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  public void subscribe(Subscriber<? super int[]> s) {
    serialFlow.subscribe(s);
  }

  @Override
  public void request(long n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void cancel() {
    serialFlow.subscribe().dispose();
    fileSubscription.dispose();
  }
}