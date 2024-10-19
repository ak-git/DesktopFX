package com.ak.fx.desktop;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.FixedFrameBytesInterceptor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.ByteOrder;

@Component
@Profile("loopback")
public final class LoopbackViewController extends AbstractScheduledViewController<BufferFrame, BufferFrame, ADCVariable> {
  private static final int FREQUENCY = 5;

  public LoopbackViewController() {
    super(
        () -> new FixedFrameBytesInterceptor("loopback", BytesInterceptor.BaudRate.BR_460800, 1 + Integer.BYTES),
        () -> new ToIntegerConverter<>(ADCVariable.class, FREQUENCY),
        FREQUENCY
    );
  }

  @Override
  public BufferFrame get() {
    return new BufferFrame(new byte[] {(byte) 0xAA, 0, 0, 1, 0}, ByteOrder.LITTLE_ENDIAN);
  }
}
