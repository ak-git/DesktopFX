package com.ak.fx.desktop.briko;

import javax.inject.Named;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.briko.BrikoConverter;
import com.ak.comm.converter.briko.BrikoVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;

@Named
@Profile("briko")
public final class BrikoViewController extends AbstractViewController<BufferFrame, BufferFrame, BrikoVariable> {
  public BrikoViewController() {
    super(() -> new RampBytesInterceptor("Briko-Stand",
        BytesInterceptor.BaudRate.BR_921600, 32), BrikoConverter::new
    );
  }
}