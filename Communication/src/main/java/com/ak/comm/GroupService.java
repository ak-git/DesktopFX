package com.ak.comm;

import java.io.File;
import java.io.FileFilter;
import java.util.stream.Stream;

import javax.inject.Provider;

import com.ak.comm.core.Service;
import com.ak.comm.file.AutoFileService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import rx.Observable;

public final class GroupService<RESPONSE, REQUEST> implements Service<RESPONSE>, FileFilter {
  private final CycleSerialService<RESPONSE, REQUEST> serialService;
  private final AutoFileService<RESPONSE, REQUEST> fileService;

  public GroupService(int baudRate, Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider) {
    serialService = new CycleSerialService<>(interceptorProvider.get());
    fileService = new AutoFileService<>(interceptorProvider.get());
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