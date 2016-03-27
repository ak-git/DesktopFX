package com.ak.fx.desktop;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.ak.comm.serial.CycleSerialService;
import com.ak.fx.scene.MilliGrid;
import com.ak.hardware.tnmi.comm.interceptor.TnmiRequest;
import com.ak.hardware.tnmi.comm.interceptor.TnmiResponse;
import rx.Observer;

public final class Controller {
  public MilliGrid root;
  private final CycleSerialService<TnmiResponse, TnmiRequest> service;

  @Inject
  public Controller(CycleSerialService<TnmiResponse, TnmiRequest> service) {
    this.service = service;
    service.getBufferObservable().subscribe(new Observer<TnmiResponse>() {
      @Override
      public void onCompleted() {
      }

      @Override
      public void onError(Throwable e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
      }

      @Override
      public void onNext(TnmiResponse tnmiResponse) {
        Logger.getLogger(getClass().getName()).info(tnmiResponse.toString());
      }
    });
  }
}
