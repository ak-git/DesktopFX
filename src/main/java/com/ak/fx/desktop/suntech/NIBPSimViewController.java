package com.ak.fx.desktop.suntech;

import com.ak.comm.bytes.suntech.NIBPRequest;
import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.suntech.NIBPVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.ak.comm.bytes.suntech.NIBPRequest.CONTROL_PNEUMATICS_ALL_CLOSED;

@Component
@Profile("suntech-test")
public final class NIBPSimViewController extends AbstractNIBPViewController {
  @Inject
  @ParametersAreNonnullByDefault
  public NIBPSimViewController(Provider<BytesInterceptor<NIBPRequest, NIBPResponse>> interceptorProvider,
                               Provider<Converter<NIBPResponse, NIBPVariable>> converterProvider) {
    super(interceptorProvider, converterProvider);
  }

  @Override
  public void refresh(boolean force) {
    super.refresh(force);
    service().write(CONTROL_PNEUMATICS_ALL_CLOSED);
  }
}
