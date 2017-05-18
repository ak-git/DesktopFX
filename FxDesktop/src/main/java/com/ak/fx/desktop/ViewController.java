package com.ak.fx.desktop;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.ak.comm.GroupService;

public final class ViewController extends AbstractViewController {
  @Inject
  public ViewController(@Nonnull GroupService<?, ?, ?> service) {
    super(service);
    service.subscribe(values -> Logger.getLogger(getClass().getName()).log(Level.INFO, Arrays.toString(values)));
  }
}
