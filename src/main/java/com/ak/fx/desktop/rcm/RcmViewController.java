package com.ak.fx.desktop.rcm;

import javax.inject.Named;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.rcm.RcmConverter;
import com.ak.comm.converter.rcm.RcmOutVariable;
import com.ak.comm.interceptor.rcm.RcmBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;

@Named
@Profile("rcm")
public final class RcmViewController
    extends AbstractViewController<BufferFrame, BufferFrame, RcmOutVariable> {
  public RcmViewController() {
    super(
        new GroupService<>(
            RcmBytesInterceptor::new,
            () -> new LinkedConverter<>(new RcmConverter(), RcmOutVariable.class)
        )
    );
  }
}
