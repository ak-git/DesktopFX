package com.ak.fx.desktop.aper;

import javax.inject.Named;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.comm.converter.aper.AperOutVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;

@Named
@Profile("aper")
public final class AperViewController
    extends AbstractViewController<BufferFrame, BufferFrame, AperOutVariable> {
  public AperViewController() {
    super(
        new GroupService<>(
            () -> new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_460800, 25),
            () -> new LinkedConverter<>(new ToIntegerConverter<>(AperInVariable.class, 1000), AperOutVariable.class)
        )
    );
  }
}
