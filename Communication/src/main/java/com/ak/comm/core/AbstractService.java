package com.ak.comm.core;

import com.ak.util.FinalizerGuardian;
import rx.Observable;
import rx.subjects.PublishSubject;

public abstract class AbstractService<FROM> implements Service<FROM> {
  private final Object finalizerGuardian = new FinalizerGuardian(this);
  private final PublishSubject<FROM> bufferPublish = PublishSubject.create();

  @Override
  public final Observable<FROM> getBufferObservable() {
    return bufferPublish.asObservable();
  }

  protected final PublishSubject<FROM> bufferPublish() {
    return bufferPublish;
  }
}
