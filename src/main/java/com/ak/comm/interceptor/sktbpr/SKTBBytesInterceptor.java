package com.ak.comm.interceptor.sktbpr;

import com.ak.comm.bytes.sktbpr.SKTBRequest;
import com.ak.comm.bytes.sktbpr.SKTBResponse;
import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Profile("sktbpr")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class SKTBBytesInterceptor extends AbstractCheckedBytesInterceptor<SKTBRequest, SKTBResponse, SKTBResponse.Builder> {
  public SKTBBytesInterceptor() {
    super("SKTB-PR", BaudRate.BR_57600, new SKTBRequest.RequestBuilder(null).build(), new SKTBResponse.Builder());
  }
}
