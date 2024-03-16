package com.ak.appliance.suntech.comm.interceptor;

import com.ak.appliance.suntech.comm.bytes.NIBPRequest;
import com.ak.appliance.suntech.comm.bytes.NIBPResponse;
import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;

public final class NIBPBytesInterceptor extends AbstractCheckedBytesInterceptor<NIBPRequest, NIBPResponse, NIBPResponse.Builder> {
  public NIBPBytesInterceptor() {
    super("Suntech-NIBP", BaudRate.BR_9600, NIBPRequest.GET_CUFF_PRESSURE, new NIBPResponse.Builder());
  }
}
