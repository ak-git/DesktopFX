package com.ak.comm.interceptor.suntech;

import javax.inject.Named;

import com.ak.comm.bytes.suntech.NIBPRequest;
import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;
import org.springframework.context.annotation.Profile;

@Named
@Profile({"suntech", "suntech-test"})
public final class NIBPBytesInterceptor extends AbstractCheckedBytesInterceptor<NIBPRequest, NIBPResponse, NIBPResponse.Builder> {
  public NIBPBytesInterceptor() {
    super("Suntech-NIBP", BaudRate.BR_9600, NIBPRequest.GET_CUFF_PRESSURE, new NIBPResponse.Builder());
  }
}
