package com.ak.comm.core;

import java.nio.channels.Channel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.Immutable;

import com.ak.util.FinalizerGuardian;
import rx.Observable;
import rx.subjects.PublishSubject;

@Immutable
public abstract class AbstractService<RESPONSE> implements Service<RESPONSE>, Channel {
  private final Object finalizerGuardian = new FinalizerGuardian(this);
  private final PublishSubject<RESPONSE> bufferPublish = PublishSubject.create();

  @Nonnull
  @Override
  public final Observable<RESPONSE> getBufferObservable() {
    return bufferPublish.asObservable();
  }

  @Override
  public boolean isOpen() {
    return bufferPublish().hasObservers();
  }

  @OverridingMethodsMustInvokeSuper
  @Override
  public void close() {
    bufferPublish().onCompleted();
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
