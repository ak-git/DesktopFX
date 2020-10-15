package com.ak.fx.desktop.suntech;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Named;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.suntech.NIBPRequest;
import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.converter.suntech.NIBPConverter;
import com.ak.comm.converter.suntech.NIBPVariable;
import com.ak.comm.interceptor.suntech.NIBPBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import org.springframework.context.annotation.Profile;

import static com.ak.comm.bytes.suntech.NIBPRequest.GET_CUFF_PRESSURE;
import static com.ak.comm.converter.suntech.NIBPConverter.FREQUENCY;

@Named
@Profile("suntech")
public final class NIBPViewController extends AbstractViewController<NIBPRequest, NIBPResponse, NIBPVariable> {
  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  public NIBPViewController() {
    super(new GroupService<>(NIBPBytesInterceptor::new, NIBPConverter::new));
    executorService.scheduleAtFixedRate(() -> service().write(GET_CUFF_PRESSURE), 0, 1000 / FREQUENCY, TimeUnit.MILLISECONDS);
  }

  @Override
  public void close() {
    executorService.shutdownNow();
    super.close();
  }

  public void start() {
    service().write(NIBPRequest.START_BP);
  }

  @Override
  public void onNext(@Nonnull int[] ints) {
    super.onNext(ints);
    if (ints[NIBPVariable.IS_COMPLETED.ordinal()] == 1) {
      service().write(NIBPRequest.GET_BP_DATA);
    }
  }
}
