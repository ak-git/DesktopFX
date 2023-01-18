package com.ak.fx.desktop;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.Converter;
import com.ak.comm.interceptor.BytesInterceptor;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.ByteOrder;

@Component
@Profile("loopback")
public final class LoopbackViewController extends AbstractScheduledViewController<BufferFrame, BufferFrame, ADCVariable> {
  @Inject
  @ParametersAreNonnullByDefault
  public LoopbackViewController(Provider<BytesInterceptor<BufferFrame, BufferFrame>> interceptorProvider,
                                Provider<Converter<BufferFrame, ADCVariable>> converterProvider) {
    super(interceptorProvider, converterProvider, 5.0);
  }

  @Override
  public BufferFrame get() {
    return new BufferFrame(new byte[] {(byte) 0xAA, 0, 0, 1, 0}, ByteOrder.LITTLE_ENDIAN);
  }
}
