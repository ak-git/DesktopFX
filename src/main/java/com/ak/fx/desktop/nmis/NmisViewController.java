package com.ak.fx.desktop.nmis;

import javax.inject.Named;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.converter.nmis.NmisConverter;
import com.ak.comm.converter.nmis.NmisVariable;
import com.ak.comm.interceptor.nmis.NmisBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;

@Named
@Profile("nmis")
public final class NmisViewController extends AbstractViewController<NmisRequest, NmisResponseFrame, NmisVariable> {
  public NmisViewController() {
    super(new GroupService<>(NmisBytesInterceptor::new, NmisConverter::new));
  }
}
