package com.ak.fx.desktop;

import javax.inject.Inject;

import com.ak.comm.serial.CycleSerialService;
import com.ak.fx.scene.MilliGrid;

public final class Controller {
  public MilliGrid root;
  private final CycleSerialService service;

  @Inject
  public Controller(CycleSerialService service) {
    this.service = service;
  }
}
