package com.ak.appliance.suntech.fx.desktop;

import com.ak.appliance.suntech.comm.bytes.NIBPRequest;
import com.ak.appliance.suntech.comm.converter.NIBPConverter;
import com.ak.appliance.suntech.comm.converter.NIBPVariable;
import com.ak.appliance.suntech.comm.interceptor.NIBPBytesInterceptor;
import com.ak.util.UIConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Component
@Profile("suntech")
public final class NIBPViewController extends AbstractNIBPViewController {
  private final Executor delayedExecutor = CompletableFuture.delayedExecutor(UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
  private volatile boolean isStartBPEnable;

  public NIBPViewController() {
    super(NIBPBytesInterceptor::new, NIBPConverter::new);
  }

  @Override
  public void onNext(int[] ints) {
    super.onNext(ints);
    isStartBPEnable = ints[NIBPVariable.PRESSURE.ordinal()] < 5;
  }

  @Override
  public void refresh(boolean force) {
    super.refresh(force);
    delayedExecutor.execute(() -> {
      if (isStartBPEnable) {
        service().write(NIBPRequest.START_BP);
      }
    });
  }
}
