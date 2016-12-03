package com.ak.comm;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import com.ak.comm.core.AbstractService;
import com.ak.comm.file.FileService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.util.EmptyComponent;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public final class GroupService<RESPONSE, REQUEST> extends AbstractService<IntBuffer[]> implements FileFilter {
  @Nonnull
  private final Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider;
  @Nonnull
  private final Disposable serialSubscription;
  @Nonnull
  private Disposable fileSubscription = EmptyComponent.INSTANCE;

  @Inject
  public GroupService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider) {
    this.interceptorProvider = interceptorProvider;
    serialSubscription = Flowable.fromPublisher(new CycleSerialService<>(interceptorProvider.get())).subscribe();
  }

  @Override
  public boolean accept(File file) {
    if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
      fileSubscription.dispose();
      fileSubscription = Flowable.fromPublisher(new FileService(file.toPath())).subscribeOn(Schedulers.io()).
          flatMap((Function<ByteBuffer, Publisher<?>>) buffer -> Flowable.fromIterable(interceptorProvider.get().apply(buffer))).subscribe();
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  public void subscribe(Subscriber<? super IntBuffer[]> s) {
  }

  @Override
  public void request(long timeSample) {
  }

  @Override
  public void cancel() {
    Stream.of(serialSubscription, fileSubscription).forEach(Disposable::dispose);
  }
}