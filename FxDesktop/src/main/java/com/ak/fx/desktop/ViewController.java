package com.ak.fx.desktop;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.converter.nmis.NmisVariable;

public final class ViewController extends AbstractViewController<NmisResponseFrame, NmisRequest, NmisVariable> {
  @Inject
  public ViewController(@Nonnull GroupService<NmisResponseFrame, NmisRequest, NmisVariable> service) {
    super(service);
    service.subscribe(values -> {
    });
  }
}
