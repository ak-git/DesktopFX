package com.ak.fx.desktop.suntech;

import com.ak.comm.bytes.suntech.NIBPRequest;
import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.suntech.NIBPVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.ak.comm.bytes.suntech.NIBPRequest.GET_CUFF_PRESSURE;
import static com.ak.comm.converter.suntech.NIBPConverter.FREQUENCY;

abstract class AbstractNIBPViewController extends AbstractScheduledViewController<NIBPRequest, NIBPResponse, NIBPVariable> {
  @Inject
  @ParametersAreNonnullByDefault
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
