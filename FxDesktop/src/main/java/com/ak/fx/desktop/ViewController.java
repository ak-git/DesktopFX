package com.ak.fx.desktop;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.ak.comm.GroupService;
import com.ak.comm.converter.Variable;

public final class ViewController<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractViewController<T, R, V> {
  @Inject
  public ViewController(@Nonnull GroupService<T, R, V> service) {
    super(service);
  }
}
