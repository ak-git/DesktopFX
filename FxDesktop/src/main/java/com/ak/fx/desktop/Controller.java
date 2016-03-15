package com.ak.fx.desktop;

import javax.inject.Inject;

import com.ak.comm.serial.SingleSerialService;
import com.ak.fx.scene.MilliGrid;

public final class Controller {
  public MilliGrid root;
  private final SingleSerialService service;

  @Inject
  public Controller(SingleSerialService service) {
    this.service = service;
  }
}
