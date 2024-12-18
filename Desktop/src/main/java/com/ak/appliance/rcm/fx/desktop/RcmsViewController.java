package com.ak.appliance.rcm.fx.desktop;

import com.ak.appliance.rcm.comm.converter.RcmConverter;
import com.ak.appliance.rcm.comm.converter.RcmOutVariable;
import com.ak.appliance.rcm.comm.converter.RcmsOutVariable;
import com.ak.appliance.rcm.comm.interceptor.RcmsBytesInterceptor;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.LinkedConverter;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rcms")
public final class RcmsViewController extends AbstractViewController<BufferFrame, BufferFrame, RcmsOutVariable> {
  public RcmsViewController() {
    super(RcmsBytesInterceptor::new, () -> LinkedConverter.of(new RcmConverter(), RcmOutVariable.class).chainInstance(RcmsOutVariable.class));
  }
}