package com.ak.appliance.caliper.fx.desktop;

import com.ak.appliance.caliper.comm.converter.CaliperConverter;
import com.ak.appliance.caliper.comm.converter.CaliperVariable;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.StringBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;

@Controller
@Profile("caliper")
public final class CaliperViewController extends AbstractViewController<BufferFrame, String, CaliperVariable> {
  public CaliperViewController() {
    super(
        () -> new StringBytesInterceptor("Caliper", BytesInterceptor.BaudRate.BR_9600, 32),
        CaliperConverter::new
    );
  }
}
