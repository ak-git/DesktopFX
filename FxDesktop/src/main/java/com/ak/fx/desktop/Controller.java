package com.ak.fx.desktop;

import javax.inject.Inject;

import com.ak.comm.serial.CycleSerialService;
import com.ak.fx.scene.MilliGrid;
import com.ak.hardware.tnmi.comm.interceptor.TnmiRequest;
import com.ak.hardware.tnmi.comm.interceptor.TnmiResponse;

public final class Controller {
  public MilliGrid root;
  private final CycleSerialService<TnmiResponse, TnmiRequest> service;

  @Inject
  public Controller(CycleSerialService<TnmiResponse, TnmiRequest> service) {
    this.service = service;
  }
}
