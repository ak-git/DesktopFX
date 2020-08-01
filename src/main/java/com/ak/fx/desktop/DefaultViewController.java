package com.ak.fx.desktop;

import javax.inject.Named;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.FixedFrameBytesInterceptor;
import org.springframework.context.annotation.Profile;

@Named
@Profile("default")
public final class DefaultViewController
    extends AbstractViewController<BufferFrame, BufferFrame, ADCVariable> {
  public DefaultViewController() {
    super(
        new GroupService<>(
            () -> new FixedFrameBytesInterceptor(BytesInterceptor.BaudRate.BR_460800, 224),
            () -> new ToIntegerConverter<>(ADCVariable.class, 1000)
        )
    );
  }
}
