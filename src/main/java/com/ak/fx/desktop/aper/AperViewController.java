package com.ak.fx.desktop.aper;

import javax.inject.Named;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.FixedFrameBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;

@Named
@Profile("aper")
public final class AperViewController
    extends AbstractViewController<BufferFrame, BufferFrame, ADCVariable> {
  public AperViewController() {
    super(
        new GroupService<>(
            () -> new FixedFrameBytesInterceptor(BytesInterceptor.BaudRate.BR_460800, 224),
            () -> new ToIntegerConverter<>(ADCVariable.class, 1000)
        )
    );
  }
}
