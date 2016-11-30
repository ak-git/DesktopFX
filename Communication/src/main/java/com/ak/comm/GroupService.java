package com.ak.comm;

import java.io.File;
import java.io.FileFilter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import com.ak.comm.file.FileService;
import com.ak.comm.interceptor.BytesInterceptor;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public final class GroupService<RESPONSE, REQUEST> implements FileFilter {
  @Nonnull
  private final Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider;

  @Inject
  public GroupService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider) {
    this.interceptorProvider = interceptorProvider;
  }

  @Override
  public boolean accept(File file) {
    if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
      Flowable.fromPublisher(new FileService(file.toPath())).subscribeOn(Schedulers.io()).
          flatMap(interceptorProvider.get()).subscribe();
      return true;
    }
    else {
      return false;
    }
  }
}