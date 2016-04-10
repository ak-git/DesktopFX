package com.ak.comm.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.util.FinalizerGuardian;
import rx.Observable;
import rx.subjects.PublishSubject;

public abstract class AbstractService<RESPONSE> implements Service<RESPONSE> {
  private final Object finalizerGuardian = new FinalizerGuardian(this);
  private final PublishSubject<RESPONSE> bufferPublish = PublishSubject.create();

  @Override
  public final Observable<RESPONSE> getBufferObservable() {
    return bufferPublish.asObservable();
  }

  protected final PublishSubject<RESPONSE> bufferPublish() {
    return bufferPublish;
  }

  protected final void logErrorAndClose(Level level, String message, Exception ex) {
    Logger.getLogger(getClass().getName()).log(level, message, ex);
    bufferPublish().onError(ex);
    close();
  }
}
