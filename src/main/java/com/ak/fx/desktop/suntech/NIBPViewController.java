package com.ak.fx.desktop.suntech;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.suntech.NIBPRequest;
import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.suntech.NIBPVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.springframework.context.annotation.Profile;

import static com.ak.comm.bytes.suntech.NIBPRequest.GET_CUFF_PRESSURE;
import static com.ak.comm.converter.suntech.NIBPConverter.FREQUENCY;
import static com.ak.fx.desktop.FxApplication.isMatchEvent;

@Named
@Profile("suntech")
public final class NIBPViewController extends AbstractViewController<NIBPRequest, NIBPResponse, NIBPVariable> {
  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private volatile boolean isStartBPEnable;

  @Inject
  public NIBPViewController(@Nonnull Provider<BytesInterceptor<NIBPRequest, NIBPResponse>> interceptorProvider,
                            @Nonnull Provider<Converter<NIBPResponse, NIBPVariable>> converterProvider) {
    super(new GroupService<>(interceptorProvider::get, converterProvider::get));
    executorService.scheduleAtFixedRate(() -> service().write(GET_CUFF_PRESSURE), 0, 1000 / FREQUENCY, TimeUnit.MILLISECONDS);
  }

  @Override
  public void close() {
    executorService.shutdownNow();
    super.close();
  }

  @Override
  public void onNext(@Nonnull int[] ints) {
    super.onNext(ints);
    if (ints[NIBPVariable.IS_COMPLETED.ordinal()] == 1) {
      service().write(NIBPRequest.GET_BP_DATA);
    }
    isStartBPEnable = ints[NIBPVariable.PRESSURE.ordinal()] < 5;
  }

  public void onKey(KeyEvent keyEvent) {
    if (isMatchEvent(keyEvent, KeyCode.N) && isStartBPEnable) {
      service().write(NIBPRequest.START_BP);
    }
  }
}
