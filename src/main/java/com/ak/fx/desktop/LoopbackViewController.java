package com.ak.fx.desktop;

import java.nio.ByteOrder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.Converter;
import com.ak.comm.interceptor.BytesInterceptor;
import org.springframework.context.annotation.Profile;

@Named
@Profile("loopback")
public final class LoopbackViewController extends AbstractViewController<BufferFrame, BufferFrame, ADCVariable> {
  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  @Inject
  public LoopbackViewController(@Nonnull Provider<BytesInterceptor<BufferFrame, BufferFrame>> interceptorProvider,
                                @Nonnull Provider<Converter<BufferFrame, ADCVariable>> converterProvider) {
    super(new GroupService<>(interceptorProvider::get, converterProvider::get));
    executorService.scheduleAtFixedRate(() -> service().write(new BufferFrame(new byte[] {(byte) 0xAA, 0, 0, 1, 0}, ByteOrder.LITTLE_ENDIAN)),
        0, 1000 / 5, TimeUnit.MILLISECONDS);
  }

  @Override
  public void close() {
    executorService.shutdownNow();
    super.close();
  }
}
