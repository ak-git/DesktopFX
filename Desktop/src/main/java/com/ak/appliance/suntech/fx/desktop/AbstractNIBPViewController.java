package com.ak.appliance.suntech.fx.desktop;

import com.ak.appliance.suntech.comm.bytes.NIBPRequest;
import com.ak.appliance.suntech.comm.bytes.NIBPResponse;
import com.ak.appliance.suntech.comm.converter.NIBPVariable;
import com.ak.comm.converter.Converter;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import static com.ak.appliance.suntech.comm.bytes.NIBPRequest.GET_CUFF_PRESSURE;
import static com.ak.appliance.suntech.comm.converter.NIBPConverter.FREQUENCY;

abstract class AbstractNIBPViewController extends AbstractScheduledViewController<NIBPRequest, NIBPResponse, NIBPVariable> {
  @Inject
  AbstractNIBPViewController(Provider<BytesInterceptor<NIBPRequest, NIBPResponse>> interceptorProvider,
                             Provider<Converter<NIBPResponse, NIBPVariable>> converterProvider) {
    super(interceptorProvider, converterProvider, FREQUENCY);
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void onNext(int[] ints) {
    super.onNext(ints);
    if (ints[NIBPVariable.IS_COMPLETED.ordinal()] == 1) {
      service().write(NIBPRequest.GET_BP_DATA);
    }
  }

  @Override
  public final NIBPRequest get() {
    return GET_CUFF_PRESSURE;
  }
}
