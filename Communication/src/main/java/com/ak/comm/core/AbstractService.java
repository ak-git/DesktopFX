package com.ak.comm.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.ak.util.FinalizerGuardian;
import rx.Observable;
import rx.subjects.PublishSubject;

@Immutable
public abstract class AbstractService<RESPONSE> implements Service<RESPONSE> {
  private final Object finalizerGuardian = new FinalizerGuardian(this);
  private final PublishSubject<RESPONSE> bufferPublish = PublishSubject.create();

  @Nonnull
  @Override
  public final Observable<RESPONSE> getBufferObservable() {
    return bufferPublish.asObservable();
  }

  @Nonnull
  protected final PublishSubject<RESPONSE> bufferPublish() {
    return bufferPublish;
  }

  protected final void logErrorAndClose(@Nonnull Level level, @Nonnull String message, @Nonnull Exception ex) {
    Logger.getLogger(getClass().getName()).log(level, message, ex);
    bufferPublish().onError(ex);
    close();
  }
}
