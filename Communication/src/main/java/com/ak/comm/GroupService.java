package com.ak.comm;

import java.io.File;
import java.io.FileFilter;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.inject.Provider;

import com.ak.comm.core.Service;
import com.ak.comm.file.AutoFileReadingService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import rx.Observable;

@Immutable
public final class GroupService<RESPONSE, REQUEST> implements Service<RESPONSE>, FileFilter {
  private final CycleSerialService<RESPONSE, REQUEST> serialService;
  private final AutoFileReadingService<RESPONSE, REQUEST> fileService;

  public GroupService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider) {
    serialService = new CycleSerialService<>(interceptorProvider.get());
    fileService = new AutoFileReadingService<>(interceptorProvider.get());
  }

  @Override
  public Observable<RESPONSE> getBufferObservable() {
    return null;
  }

  @Override
  public void close() {
    Stream.of(fileService, serialService).forEach(Service::close);
  }

  @Override
  public boolean accept(File file) {
    return fileService.accept(file);
  }
}