package com.ak.comm.file;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.comm.core.AbstractService;
import com.ak.comm.interceptor.BytesInterceptor;
import rx.Observer;
import rx.observers.TestSubscriber;

public final class AutoFileService<RESPONSE, REQUEST> extends AbstractService<RESPONSE> implements FileFilter {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor;
  private volatile FileService fileService = new FileService(Paths.get(""), TestSubscriber.create());

  public AutoFileService(BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor) {
    this.bytesInterceptor = bytesInterceptor;
    bytesInterceptor.getBufferObservable().subscribe(bufferPublish());
  }

  @Override
  public void close() {
    synchronized (executor) {
      executor.shutdownNow();
      fileService.close();
      bytesInterceptor.close();
    }
  }

  @Override
  public boolean accept(File file) {
    if (file.isFile() && file.getName().toLowerCase().endsWith(".bin")) {
      fileService.close();
      executor.execute(() -> fileService = new FileService(file.toPath(), new Observer<ByteBuffer>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
          Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
        }

        @Override
        public void onNext(ByteBuffer buffer) {
          bytesInterceptor.write(buffer);
        }
      }));
      return true;
    }
    else {
      return false;
    }
  }
}