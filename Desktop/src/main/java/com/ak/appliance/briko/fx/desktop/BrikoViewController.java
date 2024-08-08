package com.ak.appliance.briko.fx.desktop;

import com.ak.appliance.briko.comm.converter.BrikoConverter;
import com.ak.appliance.briko.comm.converter.BrikoVariable;
import com.ak.appliance.briko.comm.interceptor.BrikoBytesInterceptor;
import com.ak.comm.bytes.BufferFrame;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("briko")
public final class BrikoViewController extends AbstractViewController<BufferFrame, BufferFrame, BrikoVariable> {
  public BrikoViewController() {
    super(BrikoBytesInterceptor::new, BrikoConverter::new);
  }
}
