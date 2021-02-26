package com.ak.fx.desktop;

import java.nio.ByteOrder;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.Converter;
import com.ak.comm.interceptor.BytesInterceptor;
import org.springframework.context.annotation.Profile;

@Named
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
