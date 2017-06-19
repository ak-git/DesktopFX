package com.ak.fx.desktop.aper.emg;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.aper.myo.AperEMGVariable;
import com.ak.fx.desktop.aper.AbstractAperViewController;

public final class AperEMGViewController extends AbstractAperViewController<AperEMGVariable> {
  @Inject
  public AperEMGViewController(@Nonnull GroupService<BufferFrame, BufferFrame, AperEMGVariable> service) {
    super(service);
  }
}