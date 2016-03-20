package com.ak.comm.core;

import com.ak.util.FinalizerGuardian;
import rx.Observable;
import rx.subjects.PublishSubject;

public abstract class AbstractService<T> implements Service<T> {
  private final Object finalizerGuardian = new FinalizerGuardian(this::close);
  private final PublishSubject<T> bufferPublish = PublishSubject.create();

  @Override
  public final Observable<T> getBufferObservable() {
    return bufferPublish.asObservable();
  }

  protected final PublishSubject<T> bufferPublish() {
    return bufferPublish;
  }
}
