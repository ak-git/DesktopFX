package com.ak.fx.desktop.suntech;

import com.ak.comm.bytes.suntech.NIBPRequest;
import com.ak.comm.converter.suntech.NIBPConverter;
import com.ak.comm.converter.suntech.NIBPVariable;
import com.ak.comm.interceptor.suntech.NIBPBytesInterceptor;
import com.ak.util.UIConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
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
  public void onNext(@Nonnull int[] ints) {
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
