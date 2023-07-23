package com.ak.comm.interceptor.sktbpr;

import com.ak.comm.bytes.sktbpr.SKTBRequest;
import com.ak.comm.bytes.sktbpr.SKTBResponse;
import com.ak.comm.interceptor.AbstractCheckedBytesInterceptor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Set;

@Component
@Profile("sktb-pr")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class SKTBBytesInterceptor extends AbstractCheckedBytesInterceptor<SKTBRequest, SKTBResponse, SKTBResponse.Builder> {
  public SKTBBytesInterceptor() {
    super("SKTB-PR", BaudRate.BR_57600, SKTBRequest.NONE, new SKTBResponse.Builder());
  }

  @Nonnull
  @Override
  public Set<SerialParams> getSerialParams() {
    return EnumSet.of(SerialParams.ODD_PARITY);
  }
}
