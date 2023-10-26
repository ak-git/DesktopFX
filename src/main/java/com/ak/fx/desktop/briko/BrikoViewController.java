package com.ak.fx.desktop.briko;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.briko.BrikoConverter;
import com.ak.comm.converter.briko.BrikoStage2Variable;
import com.ak.comm.interceptor.briko.BrikoBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("briko-black")
public final class BrikoViewController extends AbstractViewController<BufferFrame, BufferFrame, BrikoStage2Variable> {
  public BrikoViewController() {
    super(
        () -> new BrikoBytesInterceptor("Briko-Black-Stand"),
        () -> LinkedConverter.of(new BrikoConverter(), BrikoStage2Variable.class)
    );
  }
}
