package com.ak.comm;

import java.io.File;
import java.io.FileFilter;
import java.nio.IntBuffer;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.ak.comm.file.FileService;
import com.ak.comm.interceptor.BytesInterceptor;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public final class GroupService<RESPONSE, REQUEST> implements Publisher<IntBuffer[]>, FileFilter {
  @Nonnull
  private final Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider;
  @Nullable
  private Disposable subscribe;

  @Inject
  public GroupService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider) {
    this.interceptorProvider = interceptorProvider;
  }

  @Override
  public boolean accept(File file) {
    if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
      Optional.ofNullable(subscribe).ifPresent(Disposable::dispose);
      subscribe = Flowable.fromPublisher(new FileService(file.toPath())).subscribeOn(Schedulers.io()).
          flatMap(interceptorProvider.get()).subscribe(response -> {

          }, throwable -> Logger.getLogger(GroupService.class.getName()).log(Level.WARNING, file.toString(), throwable),
          () -> {
          });
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  public void subscribe(Subscriber<? super IntBuffer[]> s) {
  }
}