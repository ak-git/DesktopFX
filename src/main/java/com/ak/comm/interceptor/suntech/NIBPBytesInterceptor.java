package com.ak.comm.interceptor.suntech;

import com.ak.comm.bytes.suntech.NIBPRequest;
import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Profile({"suntech", "suntech-test"})
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class NIBPBytesInterceptor extends AbstractCheckedBytesInterceptor<NIBPRequest, NIBPResponse, NIBPResponse.Builder> {
  public NIBPBytesInterceptor() {
    super("Suntech-NIBP", BaudRate.BR_9600, NIBPRequest.GET_CUFF_PRESSURE, new NIBPResponse.Builder());
  }
}
