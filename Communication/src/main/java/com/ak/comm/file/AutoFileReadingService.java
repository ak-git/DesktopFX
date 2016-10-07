package com.ak.comm.file;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

import com.ak.comm.interceptor.AbstractInterceptorService;
import com.ak.comm.interceptor.BytesInterceptor;
import rx.Observer;
import rx.observers.TestSubscriber;

public final class AutoFileReadingService<RESPONSE, REQUEST> extends AbstractInterceptorService<RESPONSE, REQUEST> implements FileFilter {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  @Nonnull
  private volatile FileReadingService fileReadingService = new FileReadingService(null, TestSubscriber.create());

  public AutoFileReadingService(@Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor) {
    super(bytesInterceptor);
  }

  @Override
  public void close() {
    fileReadingService.close();
    executor.shutdownNow();
    super.close();
  }

  @Override
  public boolean accept(@Nonnull File file) {
    if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
      fileReadingService.close();
      executor.execute(() -> fileReadingService = new FileReadingService(file.toPath(), new Observer<ByteBuffer>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(ByteBuffer buffer) {
          bytesInterceptor().write(buffer);
        }
      }));
      return true;
    }
    else {
      return false;
    }
  }
}