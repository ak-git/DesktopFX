package com.ak.fx.desktop.suntech;

import com.ak.comm.converter.suntech.NIBPConverter;
import com.ak.comm.interceptor.suntech.NIBPBytesInterceptor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ak.comm.bytes.suntech.NIBPRequest.CONTROL_PNEUMATICS_ALL_CLOSED;

@Component
@Profile("suntech-test")
public final class NIBPSimViewController extends AbstractNIBPViewController {
  public NIBPSimViewController() {
    super(NIBPBytesInterceptor::new, NIBPConverter::new);
  }

  @Override
  public void refresh(boolean force) {
    super.refresh(force);
    service().write(CONTROL_PNEUMATICS_ALL_CLOSED);
  }
}
