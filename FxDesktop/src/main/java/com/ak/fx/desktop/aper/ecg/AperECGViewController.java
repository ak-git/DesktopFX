package com.ak.fx.desktop.aper.ecg;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.aper.ecg.AperECGVariable;
import com.ak.fx.desktop.aper.AbstractAperViewController;

public final class AperECGViewController extends AbstractAperViewController<AperECGVariable> {
  @Inject
  public AperECGViewController(@Nonnull GroupService<BufferFrame, BufferFrame, AperECGVariable> service) {
    super(service);
  }
}