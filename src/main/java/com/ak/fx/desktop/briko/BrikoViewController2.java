package com.ak.fx.desktop.briko;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.briko.BrikoConverter;
import com.ak.comm.converter.briko.BrikoStage2EncoderVariable;
import com.ak.comm.interceptor.briko.BrikoBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("briko2")
public final class BrikoViewController2 extends AbstractViewController<BufferFrame, BufferFrame, BrikoStage2EncoderVariable> {
  public BrikoViewController2() {
    super(BrikoBytesInterceptor::new, () -> LinkedConverter.of(new BrikoConverter(), BrikoStage2EncoderVariable.class));
  }
}
