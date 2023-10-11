package com.ak.fx.desktop.briko;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.briko.BrikoConverter;
import com.ak.comm.converter.briko.BrikoStage3Variable;
import com.ak.comm.interceptor.briko.BrikoBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("briko")
public final class BrikoViewController extends AbstractViewController<BufferFrame, BufferFrame, BrikoStage3Variable> {
  public BrikoViewController() {
    super(BrikoBytesInterceptor::new, BrikoConverter::new);
  }
}
