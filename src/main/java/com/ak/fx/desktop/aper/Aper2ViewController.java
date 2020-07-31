package com.ak.fx.desktop.aper;

import javax.inject.Named;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.aper.Aper2OutVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.comm.converter.aper.AperOutVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;

@Named
@Profile("aper2")
public final class Aper2ViewController
    extends AbstractViewController<BufferFrame, BufferFrame, Aper2OutVariable> {
  public Aper2ViewController() {
    super(
        new GroupService<>(
            () -> new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_460800, 25),
            () ->
                new LinkedConverter<>(
                    new LinkedConverter<>(
                        new ToIntegerConverter<>(AperInVariable.class, 1000), AperOutVariable.class
                    ),
                    Aper2OutVariable.class
                )
        )
    );
  }
}
