package com.ak.appliance.suntech.fx.desktop;

import com.ak.appliance.suntech.comm.converter.NIBPConverter;
import com.ak.appliance.suntech.comm.interceptor.NIBPBytesInterceptor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ak.appliance.suntech.comm.bytes.NIBPRequest.CONTROL_PNEUMATICS_ALL_CLOSED;

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
