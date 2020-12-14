package com.ak.fx.desktop.suntech;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.ak.comm.bytes.suntech.NIBPRequest;
import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.suntech.NIBPVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import org.springframework.context.annotation.Profile;

import static com.ak.comm.bytes.suntech.NIBPRequest.CONTROL_PNEUMATICS_ALL_CLOSED;

@Named
@Profile("suntech-test")
public final class NIBPSimViewController extends AbstractNIBPViewController {
  @Inject
  public NIBPSimViewController(@Nonnull Provider<BytesInterceptor<NIBPRequest, NIBPResponse>> interceptorProvider,
                               @Nonnull Provider<Converter<NIBPResponse, NIBPVariable>> converterProvider) {
    super(interceptorProvider, converterProvider);
  }

  @Override
  public void refresh() {
    super.refresh();
    service().write(CONTROL_PNEUMATICS_ALL_CLOSED);
  }
}
