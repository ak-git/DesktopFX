package com.ak.fx.desktop.suntech;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.ak.comm.bytes.suntech.NIBPRequest;
import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.suntech.NIBPVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import com.ak.util.UIConstants;
import org.springframework.context.annotation.Profile;

import static com.ak.comm.bytes.suntech.NIBPRequest.GET_CUFF_PRESSURE;
import static com.ak.comm.converter.suntech.NIBPConverter.FREQUENCY;

@Named
@Profile("suntech")
public final class NIBPViewController extends AbstractScheduledViewController<NIBPRequest, NIBPResponse, NIBPVariable> {
  private final Executor delayedExecutor = CompletableFuture.delayedExecutor(UIConstants.UI_DELAY.getSeconds(), TimeUnit.SECONDS);
  private volatile boolean isStartBPEnable;

  @Inject
  public NIBPViewController(@Nonnull Provider<BytesInterceptor<NIBPRequest, NIBPResponse>> interceptorProvider,
                            @Nonnull Provider<Converter<NIBPResponse, NIBPVariable>> converterProvider) {
    super(interceptorProvider, converterProvider, () -> GET_CUFF_PRESSURE, FREQUENCY);
  }

  @Override
  public void onNext(@Nonnull int[] ints) {
    super.onNext(ints);
    if (ints[NIBPVariable.IS_COMPLETED.ordinal()] == 1) {
      service().write(NIBPRequest.GET_BP_DATA);
    }
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
