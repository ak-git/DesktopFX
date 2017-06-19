package com.ak.fx.desktop.aper.calibration;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.fx.desktop.aper.AbstractAperViewController;

public final class AperViewController extends AbstractAperViewController<AperInVariable> {
  @Inject
  public AperViewController(@Nonnull GroupService<BufferFrame, BufferFrame, AperInVariable> service) {
    super(service);
  }
}