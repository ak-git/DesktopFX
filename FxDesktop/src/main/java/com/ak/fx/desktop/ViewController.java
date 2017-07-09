package com.ak.fx.desktop;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.ak.comm.GroupService;
import com.ak.comm.converter.Variable;

public final class ViewController<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>> extends AbstractViewController<RESPONSE, REQUEST, EV> {
  @Inject
  public ViewController(@Nonnull GroupService<RESPONSE, REQUEST, EV> service) {
    super(service);
    service.subscribe(values -> {
    });
  }
}
