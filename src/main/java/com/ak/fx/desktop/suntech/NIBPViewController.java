package com.ak.fx.desktop.suntech;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.ak.comm.bytes.suntech.NIBPRequest;
import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.suntech.NIBPVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.util.UIConstants;
import org.springframework.context.annotation.Profile;

@Named
@Profile("suntech")
public final class NIBPViewController extends AbstractNIBPViewController {
  private final Executor delayedExecutor = CompletableFuture.delayedExecutor(UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
  private volatile boolean isStartBPEnable;

  @Inject
  @ParametersAreNonnullByDefault
  public NIBPViewController(Provider<BytesInterceptor<NIBPRequest, NIBPResponse>> interceptorProvider,
                            Provider<Converter<NIBPResponse, NIBPVariable>> converterProvider) {
    super(interceptorProvider, converterProvider);
  }

  @Override
  public void onNext(@Nonnull int[] ints) {
    super.onNext(ints);
    isStartBPEnable = ints[NIBPVariable.PRESSURE.ordinal()] < 5;
  }

  @Override
  public void refresh() {
    super.refresh();
    delayedExecutor.execute(() -> {
      if (isStartBPEnable) {
        service().write(NIBPRequest.START_BP);
      }
    });
  }
}
