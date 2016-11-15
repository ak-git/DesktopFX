package com.ak.comm;

import java.io.File;
import java.io.FileFilter;
import java.nio.IntBuffer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.inject.Provider;

import com.ak.comm.core.AbstractService;
import com.ak.comm.core.Service;
import com.ak.comm.file.AutoFileReadingService;
import com.ak.comm.interceptor.BytesInterceptor;

@Immutable
public final class GroupService<RESPONSE, REQUEST> extends AbstractService<IntBuffer[]> implements FileFilter {
  private final AutoFileReadingService<RESPONSE, REQUEST> fileService;

  public GroupService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider) {
    fileService = new AutoFileReadingService<>(interceptorProvider.get());
  }

  @Override
  public void close() {
    Stream.of(fileService).forEach(Service::close);
    super.close();
  }

  @Override
  public boolean accept(File file) {
    return fileService.accept(file);
  }
}