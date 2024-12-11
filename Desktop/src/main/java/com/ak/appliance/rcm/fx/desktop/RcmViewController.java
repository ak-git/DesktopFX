package com.ak.appliance.rcm.fx.desktop;

import com.ak.appliance.rcm.comm.converter.RcmConverter;
import com.ak.appliance.rcm.comm.converter.RcmOutVariable;
import com.ak.appliance.rcm.comm.interceptor.RcmBytesInterceptor;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.LinkedConverter;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rcm")
public final class RcmViewController extends AbstractViewController<BufferFrame, BufferFrame, RcmOutVariable> {
  public RcmViewController() {
    super(RcmBytesInterceptor::new, () -> LinkedConverter.of(new RcmConverter(), RcmOutVariable.class));
  }
}
