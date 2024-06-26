package com.ak.appliance.rcm.comm.interceptor;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Profile({"rcm", "rcm-calibration"})
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class RcmBytesInterceptor extends AbstractRcmBytesInterceptor {
  public RcmBytesInterceptor() {
    super("rcm");
  }
}
